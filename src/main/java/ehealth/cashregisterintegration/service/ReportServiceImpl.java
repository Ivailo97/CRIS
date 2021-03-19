package ehealth.cashregisterintegration.service;

import java.io.FileNotFoundException;
import java.io.FileWriter;

import ehealth.cashregisterintegration.data.model.TotalReport;
import ehealth.cashregisterintegration.data.model.CashRegisterConfig;
import ehealth.cashregisterintegration.data.model.ItemReport;
import ehealth.cashregisterintegration.data.dto.SaleDTO;
import ehealth.cashregisterintegration.repository.CashRegisterConfigRepository;
import ehealth.cashregisterintegration.repository.TotalReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.nio.file.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
//@EnableScheduling
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final CashRegisterConfigRepository configRepository;

    private final TotalReportRepository totalReportRepository;

    @Override
    public void sendSellInfo(SaleDTO sale) {

        CashRegisterConfig config = this.configRepository.findAll().get(0);

        String encodedBasicAuth = HttpHeaders.encodeBasicAuth(config.getAstoreUsername(),
                config.getAstorePassword(), StandardCharsets.UTF_8);

        WebClient client = WebClient.builder()
                .baseUrl(config.getAstoreUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, encodedBasicAuth)
                .build();

        client.post()
                .uri("/report/" + config.getDeviceName())
                .bodyValue(sale)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(res -> {
                    log.info("Sale report for item: " + sale.getItemNumber() + "sync successfully!");
                }, err -> {
                    log.error(err.getMessage());
                });
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
                        TotalReport totalReport = readReport(filePath);
                        upsertTotalReport(totalReport);
                        writeCSVFile("report.csv", totalReport.getItemReports(), delimiter, quotes);
                    }
                    key.reset();
                }

            } catch (IOException | InterruptedException | IllegalArgumentException e) {
                log.error("Error: " + e.getMessage());
            }

        });
    }


    //    @Scheduled(fixedRate = 5000)
    public void sendDailyReports() {
        TotalReport dailyReport = calculateDailyReport();

        //if daily report exist
        if (dailyReport.getOperator() != null) {

            totalReportRepository.findByDate(dailyReport.getDate())
                    .ifPresentOrElse((reportInDB) -> {

                        CashRegisterConfig config = configRepository.findAll().get(0);

                        String encodedBasicAuth = HttpHeaders.encodeBasicAuth(config.getAstoreUsername(),
                                config.getAstorePassword(), StandardCharsets.UTF_8);

                        WebClient client = WebClient.builder()
                                .baseUrl(config.getAstoreUrl())
                                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .defaultHeader(HttpHeaders.AUTHORIZATION, encodedBasicAuth)
                                .build();

                        client.post()
                                .uri("/report/" + config.getDeviceName())
                                .bodyValue(dailyReport)
                                .retrieve()
                                .bodyToMono(String.class)
                                .subscribe(res -> {
                                    log.info("Daily report for " + dailyReport.getDate() + "sync successfully!");
                                    reportInDB.setDailySync(true);
                                }, err -> {
                                    log.error(err.getMessage());
                                    reportInDB.setDailySync(false);
                                }, () -> totalReportRepository.save(reportInDB));

                    }, () -> log.error("Daily report date is ahead of time"));
        }
    }

    //must be ready till the send time
    //if today report is not ready logs warning message
    private TotalReport calculateDailyReport() {
        Instant now = Instant.now();
        Instant yesterday = now.minus(1, ChronoUnit.DAYS);

        TotalReport dailyReport = TotalReport.builder()
                .date(getDateAsString(now))
                .build();

        totalReportRepository.findByDate(getDateAsString(now))
                .ifPresentOrElse(todayReport -> {
                    dailyReport.setOperator(todayReport.getOperator());
                    totalReportRepository.findByDate(getDateAsString(yesterday))
                            .ifPresentOrElse(yesterdayReport -> dailyReport.setItemReports(calcDailyItems(yesterdayReport, todayReport)),
                                    () -> dailyReport.setItemReports(todayReport.getItemReports()));
                }, () -> log.warn("Sync the daily total report first"));

        return dailyReport;
    }

    //items for the day remains
    //removes yesterday items from today items and add the new items from today
    private List<ItemReport> calcDailyItems(TotalReport yesterdayReport, TotalReport todayReport) {
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

    private void upsertTotalReport(TotalReport totalReport) {
        totalReportRepository.findByDate(totalReport.getDate())
                .ifPresentOrElse(oldReport -> {
                    totalReportRepository.delete(oldReport);
                    TotalReport updated = totalReportRepository.save(totalReport);
                    log.info("Updated total report for " + totalReport.getDate());
                    log.info("Before update: " + oldReport.toString());
                    log.info("After update: " + updated.toString());
                }, () -> {
                    TotalReport created = totalReportRepository.save(totalReport);
                    log.info("Created total report for " + totalReport.getDate());
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

    private static TotalReport readReport(String path) {
        String[] lines = readFile(path).split(System.lineSeparator());

        String dateLine = lines[lines.length - 5];
        String operatorLine = lines[3];

        String[] itemLines = Arrays.stream(lines).skip(5)
                .takeWhile(line -> !line.matches("^\\d+.+$"))
                .toArray(String[]::new);

        String[][] itemChunkLines = chunkArray(itemLines, 4);

        String date = getLineKVP(dateLine)[1];
        String operator = getLineKVP(operatorLine)[2];

        return new TotalReport(operator, date, readItemReports(itemChunkLines), false);
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

    public static String[][] chunkArray(String[] array, int chunkSize) {
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
