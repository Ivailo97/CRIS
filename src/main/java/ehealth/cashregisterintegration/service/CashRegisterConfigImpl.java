package ehealth.cashregisterintegration.service;

import ehealth.cashregisterintegration.data.dto.DeviceConfigDTO;
import ehealth.cashregisterintegration.data.dto.DeviceDTO;
import ehealth.cashregisterintegration.data.model.CashRegisterConfig;
import ehealth.cashregisterintegration.data.model.Department;
import ehealth.cashregisterintegration.data.model.Item;
import ehealth.cashregisterintegration.data.rest.ApiResponse;
import ehealth.cashregisterintegration.repository.CashRegisterConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashRegisterConfigImpl implements CashRegisterConfigService {

    private final CashRegisterConfigRepository repository;

    private final ModelMapper mapper;

    @Override
    public ApiResponse setConfig(DeviceConfigDTO config) {

        repository.deleteAll();

        CashRegisterConfig cashRegisterConfig = mapper.map(config, CashRegisterConfig.class);

        List<Department> departments = initDepartments(config);
        List<Item> items = initItems(config, departments);

        cashRegisterConfig.setDepartments(departments);
        cashRegisterConfig.setItems(items);

        cashRegisterConfig = repository.save(cashRegisterConfig);

        return ApiResponse.builder()
                .message("Configured for: " + cashRegisterConfig.getDeviceName())
                .build();
    }

    @Override
    public DeviceDTO getDevice() {
        CashRegisterConfig deviceConfig = repository.findAll().get(0);

        DeviceDTO device = null;

        if (deviceConfig != null) {
            device = mapper.map(deviceConfig, DeviceDTO.class);
        }

        return device;
    }

    private List<Item> initItems(DeviceConfigDTO config, List<Department> departments) {
        return config.getItems().stream().map(itemDTO -> {
            Item item = mapper.map(itemDTO, Item.class);
            departments.stream()
                    .filter(d -> d.getNumber().equals(itemDTO.getDepartment()))
                    .findAny()
                    .ifPresent(item::setDepartment);
            return item;
        }).collect(Collectors.toList());
    }

    private List<Department> initDepartments(DeviceConfigDTO config) {
        List<Department> departments = new ArrayList<>();

        for (int i = 1; i <= 50; i++) {
            AtomicReference<Department> department = new AtomicReference<>();
            int number = i;

            config.getDepartments().stream()
                    .filter(x -> x.getNumber().equals(number))
                    .findFirst()
                    .ifPresentOrElse(depDTO -> {
                                Department dep = mapper.map(depDTO, Department.class);
                                dep.setNumber(number);
                                department.set(dep);
                            },
                            () -> department.set(Department.builder()
                                    .number(number)
                                    .name("Департамент " + number)
                                    .maxDigits(9)
                                    .taxGroup("A")
                                    .build()));

            departments.add(department.get());
        }

        return departments;
    }
}
