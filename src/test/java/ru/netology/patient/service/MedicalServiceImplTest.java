package ru.netology.patient.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoFileRepository;
import ru.netology.patient.service.alert.SendAlertService;
import ru.netology.patient.service.medical.MedicalServiceImpl;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class MedicalServiceImplTest {
    private MedicalServiceImpl medicalService;
    private PatientInfoFileRepository patientInfoFileRepository;
    private SendAlertService alertService;

    String patienId;
    PatientInfo patientInfo;

    @BeforeEach
    public void setUp() {
        patientInfoFileRepository = mock(PatientInfoFileRepository.class);
        alertService = mock(SendAlertService.class);
        medicalService = new MedicalServiceImpl(patientInfoFileRepository, alertService);
        patienId = "125";
        HealthInfo healthInfo = new HealthInfo(new BigDecimal(36.6), new BloodPressure(120, 80));
        patientInfo = new PatientInfo(patienId, null, null, null, healthInfo);
    }

    public void sendAlarmMessage(String patsienId) {
        // создаём объект ArgumentCaptor, который будет "захватывать" аргументы, переданные в метод send();
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        // используем метод verify() из Mockito, чтобы проверить, был ли вызван метод send() у объекта alertService
        verify(alertService, times(1)).send(argumentCaptor.capture());
        // cравнение захваченного значения с ожидаемым
        assertEquals(String.format("Warning, patient with id: %s, need help", patsienId), argumentCaptor.getValue());
    }

    @Test
    public void testCheckForAbnormalBloodPressure() {
        // Arrange
        BloodPressure abnormalPressure = new BloodPressure(150, 90);
        when(patientInfoFileRepository.getById(patienId)).thenReturn(patientInfo);

        //Act
        medicalService.checkBloodPressure(patienId, abnormalPressure);

        //Assert
        sendAlarmMessage(patienId);
    }

    @Test
    public void testCheckForLowOrForTemperature() {
        // Arrange
        BigDecimal temperature = new BigDecimal(39);
        when(patientInfoFileRepository.getById(patienId)).thenReturn(patientInfo);

        //Act
        medicalService.checkTemperature(patienId, temperature);

        //Assert
        sendAlarmMessage(patienId);
    }

    @Test
    public void testCheckForNoMessageWithNormalBloodPressure() {
        // Arrange
        BloodPressure normalPressure = new BloodPressure(120,80);
        when(patientInfoFileRepository.getById(patienId)).thenReturn(patientInfo);

        //Act
        medicalService.checkBloodPressure(patienId, normalPressure);

        //Assert
        // используем метод verify() чтобы проверить, что метод send() не был вызван у объекта alertService
        verify(alertService, never()).send(anyString());
    }

    @Test
    public void checkForNoMessageWithNormalTemperature () {
        // Arrange
        BigDecimal temperature = new BigDecimal(36.6);
        when(patientInfoFileRepository.getById(patienId)).thenReturn(patientInfo);

        //Act
        medicalService.checkTemperature(patienId,temperature);

        //Assert
        verify(alertService, never()).send(anyString());
    }
}