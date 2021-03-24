package ehealth.cashregisterintegration.service;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import ehealth.cashregisterintegration.data.device.request.*;
import ehealth.cashregisterintegration.data.device.response.DaisyResponse;
import ehealth.cashregisterintegration.data.dto.*;
import ehealth.cashregisterintegration.data.model.CashRegisterConfig;
import ehealth.cashregisterintegration.repository.CashRegisterConfigRepository;
import ehealth.cashregisterintegration.utils.SaleUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private static final String DEVICE_APP_API_BASE = "http://localhost:7000/api";
    private static final String USB_COM_PORT = "COM3";
    private static final String DEFAULT_CMD_TYPE = "CmdCOMPort";

    private static final String START_RCP = "FDStartFiscRcp";
    private static final String SALE_ITEM = "FDSaleItem";
    private static final String RCP_STATUS = "FDFiscRcpStatus";
    private static final String TOTAL_SUM = "FDTotalSum";
    private static final String END_RCP = "FDEndFiscRcp";
    private static final String DAILY_RPT = "FDDailyRpt";
    private static final String PAPER_FEED = "FDPaperFeed";

    private static final String DEFAULT_TAX_GROUP = "А";
    private static final String DEFAULT_STATUS_OPTION = "T";
    private static final String DEFAULT_SALE_SIGN = "+";
    private static final Integer DEFAULT_OPERATOR = 1;
    private static final Integer DEFAULT_NETTO = 0;
    private static final String PAYMENT = "P"; // в брой

    private CashRegisterConfig currentConfig;

    private final CashRegisterConfigRepository repository;

    private final ReportService reportService;

    private final BCryptPasswordEncoder encoder;

    private final SaleUtils saleUtils;

    private final ModelMapper mapper;

    private final Gson gson;

    @PostConstruct
    private void init() {
        if (repository.count() != 0) {
            loadConfig();
        }
    }

    @Override
    public void loadConfig() {
        log.info("Configuration loaded successful!");
        currentConfig = repository.findAll().get(0);
    }

    @Override
    public ApiResponseDTO setConfig(DeviceConfigDTO config) {
        repository.deleteAll();
        config.getCredentials().setPassword(encoder.encode(config.getCredentials().getPassword()));
        CashRegisterConfig cashRegisterConfig = mapper.map(config, CashRegisterConfig.class);
        cashRegisterConfig = repository.save(cashRegisterConfig);

        return ApiResponseDTO.builder()
                .message("Configured for: " + cashRegisterConfig.getDeviceName())
                .build();
    }

    @Override
    public DaisyResponse sell(FreeSaleDTO sale) {
        return check(() -> {
            String unicSaleNum = saleUtils.formSaleNumber(currentConfig.getDeviceName(), sale);

            StartFiscRcpData startFiscRcpData = new StartFiscRcpData(DEFAULT_OPERATOR, DEFAULT_OPERATOR,
                    unicSaleNum, Strings.EMPTY, Strings.EMPTY);

            List<ReqCommandWrapper> freeSalesCommands = formSaleCommands(sale);
            StatusData statusData = new StatusData(DEFAULT_STATUS_OPTION);
            BigDecimal amountIn = saleUtils.calculateTotal(sale);
            TotalSumData totalSumData = new TotalSumData(Strings.EMPTY, Strings.EMPTY, PAYMENT, amountIn);

            List<ReqCommandWrapper> commandSequence = new ArrayList<>(
                    List.of(generateReqCommandWrapper(START_RCP, startFiscRcpData)));
            commandSequence.addAll(freeSalesCommands);
            commandSequence.add(generateReqCommandWrapper(RCP_STATUS, statusData));
            commandSequence.add(generateReqCommandWrapper(TOTAL_SUM, totalSumData));
            commandSequence.add(generateReqCommandWrapper(END_RCP, null));

            return sendCommandSequence(commandSequence.toArray(ReqCommandWrapper[]::new), sale);
        });
    }

    @Override
    public DaisyResponse zDailyReport() {
        return check(() -> {
            DailyReportData cmdData = new DailyReportData(0, Strings.EMPTY);
            ReqCommandWrapper cmdWrapper = generateReqCommandWrapper(DAILY_RPT, cmdData);
            DaisyRequest request = generateDaisyRequest(new ReqCommandWrapper[]{cmdWrapper});
            return sendDaisyRequest(request);
        });
    }

    @Override
    public DaisyResponse paperFeed(Integer count) {
        return check(() -> {
            PaperFeedData cmdData = new PaperFeedData(count);
            ReqCommandWrapper cmdWrapper = generateReqCommandWrapper(PAPER_FEED, cmdData);
            DaisyRequest request = generateDaisyRequest(new ReqCommandWrapper[]{cmdWrapper});
            return sendDaisyRequest(request);
        });
    }

    private List<ReqCommandWrapper> formSaleCommands(FreeSaleDTO sale) {
        //sets every item tax group to default
        for (ItemSaleDTO item : sale.getItems()) {
            item.setTaxGroup(DEFAULT_TAX_GROUP);
        }

        return Arrays.stream(sale.getItems())
                .map(sdto -> {
                    FreeSaleData freeSaleData = mapper.map(sdto, FreeSaleData.class);
                    freeSaleData.setSign(DEFAULT_SALE_SIGN);
                    freeSaleData.setQty(sdto.getQuantity());
                    freeSaleData.setNetto(DEFAULT_NETTO);
                    freeSaleData.setTaxGrp(sdto.getTaxGroup());
                    return generateReqCommandWrapper(SALE_ITEM, freeSaleData);
                }).collect(Collectors.toList());
    }

    private DaisyResponse check(Supplier<DaisyResponse> supplier) {
        if (currentConfig != null) {
            return supplier.get();
        } else {
            return new DaisyResponse();
        }
    }

    private ReqCommandWrapper generateReqCommandWrapper(String reqName, Object cmdData) {
        return new ReqCommandWrapper(new ReqCommand(reqName, cmdData));
    }

    private DaisyRequest generateDaisyRequest(ReqCommandWrapper[] reqCommands) {
        Cmd cmd = new Cmd(USB_COM_PORT, reqCommands);
        WebSrvCmd webSrvCmd = new WebSrvCmd(DEFAULT_CMD_TYPE, cmd);
        return new DaisyRequest(webSrvCmd);
    }

    private DaisyResponse sendDaisyRequest(DaisyRequest request, FreeSaleDTO... sale) {
        DaisyResponse response;
        try {

            HttpResponse<JsonNode> jsonNode = Unirest.post(DEVICE_APP_API_BASE)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(new JsonNode(gson.toJson(request)))
                    .asJson();

            String jsonResponse = jsonNode.getBody().getObject().toString();
            response = gson.fromJson(jsonResponse, DaisyResponse.class);

            String firstCommand = request.getWebSrvCmd()
                    .getCmd()
                    .getCOMPortMsgList()[0]
                    .getReqCommand()
                    .getCmd();

            if (firstCommand.equals(START_RCP) && !response.getWebSrvCmd().isHasErr()) {
                FreeSaleDTO saleDTO = Arrays.stream(sale).toArray(FreeSaleDTO[]::new)[0];
                reportService.saveAndSendSaleInfo(currentConfig, saleDTO);
            }

        } catch (UnirestException ex) {
            response = new DaisyResponse();
        }

        return response;
    }

    private DaisyResponse sendCommandSequence(ReqCommandWrapper[] cmdWrappers, FreeSaleDTO... sales) {
        DaisyRequest request = generateDaisyRequest(cmdWrappers);
        return sendDaisyRequest(request, sales);
    }
}
