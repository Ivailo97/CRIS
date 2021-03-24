package ehealth.cashregisterintegration.service;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import ehealth.cashregisterintegration.data.dto.FreeSaleDTO;
import ehealth.cashregisterintegration.data.dto.ReportDTO;
import ehealth.cashregisterintegration.data.model.CashRegisterConfig;
import ehealth.cashregisterintegration.data.model.ItemSale;
import ehealth.cashregisterintegration.data.model.Sale;
import ehealth.cashregisterintegration.data.model.listen.DailyReport;
import ehealth.cashregisterintegration.data.model.listen.ItemReport;
import ehealth.cashregisterintegration.repository.CashRegisterConfigRepository;
import ehealth.cashregisterintegration.repository.DailyReportRepository;
import ehealth.cashregisterintegration.repository.SaleRepository;
import ehealth.cashregisterintegration.utils.SaleUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
//@EnableScheduling
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final CashRegisterConfigRepository configRepository;

    private final DailyReportRepository dailyReportRepository;

    private final SaleRepository saleRepository;

    private final SaleUtils saleUtils;

    private final ModelMapper mapper;

    private final Gson gson;

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    public void saveAndSendSaleInfo(CashRegisterConfig config, FreeSaleDTO saleDTO) {

        BigDecimal total = saleUtils.calculateTotal(saleDTO);
        ReportDTO report = new ReportDTO(config.getDeviceName(),config.getLocation(), saleDTO, total);
        report.getSale().setNumber(saleUtils.formSaleNumber(config.getDeviceName(), saleDTO));

        List<ItemSale> itemSales = Arrays.stream(report.getSale().getItems())
                .map(itemSaleDTO -> mapper.map(itemSaleDTO, ItemSale.class))
                .collect(Collectors.toList());

        Date date = getDate(report);

        Sale sale = Sale.builder()
                .total(report.getTotal())
                .date(date)
                .items(itemSales)
                .build();

        String encodedBasicAuth = HttpHeaders.encodeBasicAuth(config.getCredentials().getUsername(),
                config.getCredentials().getPassword(), StandardCharsets.UTF_8);

        try {
            log.info("Sending report to accounting service...");
            HttpResponse<JsonNode> response = Unirest.post(config.getAccountingServiceUrl() + "/report")
                    .header(HttpHeaders.AUTHORIZATION, encodedBasicAuth)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(gson.toJson(report))
                    .asJson();

            if (response.getStatus() == HttpStatus.OK.value() || response.getStatus() == HttpStatus.CREATED.value()) {
                sale.setSync(true);
                log.info("Report successfully sync...");
            }

        } catch (UnirestException e) {
            log.error("Error " + e.getMessage());
        }

        saleRepository.save(sale);
    }

    private Date getDate(ReportDTO report) {
        Date date;
        try {
            date = dateFormatter.parse(report.getSale().getDate());
        } catch (ParseException e) {
            date = new Date();
            log.error("Cant parse date");
        }
        return date;
    }

    @Override
    public void listenForTotalReports(String delimiter, Boolean quotes) {

        CompletableFuture.runAsync(() -> {

            try {
                CashRegisterConfig config = configRepository.findAll().get(0);

                WatchService watchService = FileSystems.getDefault().newWatchService();
                Path path = Paths.get(config.getDirToListen());

                path.register(
                        watchService,
                        StandardWatchEventKinds.ENTRY_CREATE);
//                    StandardWatchEventKinds.ENTRY_MODIFY);

                log.info("Listening for report text files creation in " + config.getDirToListen() + "...");

                WatchKey key;
                while ((key = watchService.take()) != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        log.info("Event kind: " + event.kind() + ". File affected: " + event.context() + ".");
                        String filePath = config.getDirToListen() + "\\" + event.context();
                        DailyReport dailyReport = readReport(filePath);
                        upsertTotalReport(dailyReport);
                        writeCSVFile("report.csv", dailyReport.getItemReports(), delimiter, quotes);
                    }
                    key.reset();
                }

            } catch (IOException | InterruptedException | IllegalArgumentException e) {
                log.error("Error: " + e.getMessage());
            }

        });
    }

    // @Scheduled(fixedRate = 5000)
    public void sendDailyReports() {
        DailyReport dailyReport = calculateDailyReport();

        //if daily report exist
        if (dailyReport.getOperator() != null) {

            dailyReportRepository.findByDate(dailyReport.getDate())
                    .ifPresentOrElse((reportInDB) -> {

                        CashRegisterConfig config = configRepository.findAll().get(0);

//                        String encodedBasicAuth = HttpHeaders.encodeBasicAuth(config.getAstoreUsername(),
//                                config.getAstorePassword(), StandardCharsets.UTF_8);

//                        WebClient client = WebClient.builder()
//                                .baseUrl(config.getAstoreUrl())
//                                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                                .defaultHeader(HttpHeaders.AUTHORIZATION, encodedBasicAuth)
//                                .build();
//
//                        client.post()
//                                .uri("/report/" + config.getDeviceName())
//                                .bodyValue(dailyReport)
//                                .retrieve()
//                                .bodyToMono(String.class)
//                                .subscribe(res -> {
//                                    log.info("Daily report for " + dailyReport.getDate() + "sync successfully!");
//                                    reportInDB.setDailySync(true);
//                                }, err -> {
//                                    log.error(err.getMessage());
//                                    reportInDB.setDailySync(false);
//                                }, () -> dailyReportRepository.save(reportInDB));

                    }, () -> log.error("Daily report date is ahead of time"));
        }
    }

    //must be ready till the send time
    //if today report is not ready logs warning message
    private DailyReport calculateDailyReport() {
        Instant now = Instant.now();
        Instant yesterday = now.minus(1, ChronoUnit.DAYS);

        DailyReport dailyReport = DailyReport.builder()
                .date(getDateAsString(now))
                .build();

        dailyReportRepository.findByDate(getDateAsString(now))
                .ifPresentOrElse(todayReport -> {
                    dailyReport.setOperator(todayReport.getOperator());
                    dailyReportRepository.findByDate(getDateAsString(yesterday))
                            .ifPresentOrElse(yesterdayReport -> dailyReport.setItemReports(calcDailyItems(yesterdayReport, todayReport)),
                                    () -> dailyReport.setItemReports(todayReport.getItemReports()));
                }, () -> log.warn("Sync the daily total report first"));

        return dailyReport;
    }

    //items for the day remains
    //removes yesterday items from today items and add the new items from today
    private List<ItemReport> calcDailyItems(DailyReport yesterdayReport, DailyReport todayReport) {
        List<ItemReport> dailyItemsReport = new ArrayList<>();

        for (ItemReport yesterdayItemsReport : yesterdayReport.getItemReports()) {
            //it will always be present
            todayReport.getItemReports().stream()
                    .filter(i -> i.getName().equals(yesterdayItemsReport.getName()))
                    .findFirst()
                    .ifPresent(report -> {
                        ItemReport dailyItemReport = ItemReport.builder()
                                .name(report.getName())
                                .number(report.getNumber())
                                .quantity(report.getQuantity().subtract(yesterdayItemsReport.getQuantity()))
                                .sum(report.getSum().subtract(yesterdayItemsReport.getSum()))
                                .build();
                        dailyItemsReport.add(dailyItemReport);
                    });
        }

        //add the new items from today
        todayReport.getItemReports()
                .stream()
                .filter(ip -> dailyItemsReport.stream().noneMatch(itr -> ip.getName().equals(itr.getName())))
                .forEach(dailyItemsReport::add);

        return dailyItemsReport;
    }

    private String getDateAsString(Instant instant) {
        Date yesterdayDate = Date.from(instant);
        String formattedYesterdayDate = DateFormat.getDateInstance().format(yesterdayDate);
        return formattedYesterdayDate.substring(0, formattedYesterdayDate.length() - 3);
    }

    private void upsertTotalReport(DailyReport dailyReport) {
        dailyReportRepository.findByDate(dailyReport.getDate())
                .ifPresentOrElse(oldReport -> {
                    dailyReportRepository.delete(oldReport);
                    DailyReport updated = dailyReportRepository.save(dailyReport);
                    log.info("Updated total report for " + dailyReport.getDate());
                    log.info("Before update: " + oldReport.toString());
                    log.info("After update: " + updated.toString());
                }, () -> {
                    DailyReport created = dailyReportRepository.save(dailyReport);
                    log.info("Created total report for " + dailyReport.getDate());
                    log.info("Details: " + created.toString());
                });
    }

    private static void writeCSVFile(String csvFileName,
                                     List<ItemReport> reports,
                                     String delimiter,
                                     boolean valueQuotes) {

        try {
            FileWriter csvWriter = new FileWriter(csvFileName);
            csvWriter
                    .append("Number")
                    .append(delimiter)
                    .append("Name")
                    .append(delimiter)
                    .append("Quantity")
                    .append(delimiter)
                    .append("Sum")
                    .append(System.lineSeparator());

            for (ItemReport itemReport : reports) {
                csvWriter
                        .append(valueQuotes ? String.format("'%s'", itemReport.getNumber()) : itemReport.getNumber())
                        .append(delimiter)
                        .append(valueQuotes ? String.format("'%s'", itemReport.getName()) : itemReport.getName())
                        .append(delimiter)
                        .append(valueQuotes ? String.format("'%s'", itemReport.getQuantity()) : String.valueOf(itemReport.getQuantity()))
                        .append(delimiter)
                        .append(valueQuotes ? String.format("'%s'", itemReport.getSum()) : String.valueOf(itemReport.getSum()))
                        .append(System.lineSeparator());
            }

            csvWriter.flush();
            csvWriter.close();

        } catch (IOException ex) {
            log.error("Cannot write csv file..");
        }
    }

    private static DailyReport readReport(String path) {
        String[] lines = readFile(path).split(System.lineSeparator());

        String dateLine = lines[lines.length - 5];
        String operatorLine = lines[3];

        String[] itemLines = Arrays.stream(lines).skip(5)
                .takeWhile(line -> !line.matches("^\\d+.+$"))
                .toArray(String[]::new);

        String[][] itemChunkLines = chunkArray(itemLines, 4);

        String date = getLineKVP(dateLine)[1];
        String operator = getLineKVP(operatorLine)[2];

        return new DailyReport(operator, date, readItemReports(itemChunkLines), false);
    }

    private static List<ItemReport> readItemReports(String[][] itemChunkLines) {

        List<ItemReport> itemReports = new ArrayList<>();

        for (String[] itemChunk : itemChunkLines) {
            String[] numberKVP = getLineKVP(itemChunk[0]);
            String[] nameKVP = getLineKVP(itemChunk[1]);
            String[] quantityKVP = getLineKVP(itemChunk[2]);
            String[] sumKVP = getLineKVP(itemChunk[3]);
            String number = numberKVP[1];
            String name = nameKVP[0].substring(0, nameKVP[0].length() - 2);
            BigDecimal quantity = new BigDecimal(quantityKVP[1]);
            BigDecimal sum = new BigDecimal(sumKVP[1]);
            ItemReport itemReport = new ItemReport(number, name, quantity, sum);
            itemReports.add(itemReport);
        }

        return itemReports;
    }

    private static String[] getLineKVP(String line) {
        return Arrays.stream(line.split("[:\\s]+"))
                .filter(x -> !x.isEmpty() && !x.isBlank())
                .toArray(String[]::new);
    }

    private static String readFile(String path) {
        StringBuilder data = new StringBuilder();
        try {
            File myObj = new File(path);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                data.append(myReader.nextLine()).append(System.lineSeparator());
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return data.toString();
    }

    private static String[][] chunkArray(String[] array, int chunkSize) {
        int numOfChunks = (int) Math.ceil((double) array.length / chunkSize);
        String[][] output = new String[numOfChunks][];

        for (int i = 0; i < numOfChunks; ++i) {
            int start = i * chunkSize;
            int length = Math.min(array.length - start, chunkSize);

            String[] temp = new String[length];
            System.arraycopy(array, start, temp, 0, length);
            output[i] = temp;
        }

        return output;
    }
}
