package Enrollee;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import com.opencsv.exceptions.CsvException;


public class EnrolleeProcessor {

    public static void main(String[] args) throws CsvException {
        String inputFilePath = "/Enrollee/src/main/java/Enrollee/input.csv";  // Replace with your actual input file path
        processCsv(inputFilePath);
    }

    public static void processCsv(String inputFilePath) throws CsvException {
        Map<String, List<Map<String, String>>> recordsByCompany = new HashMap<>();

        try (CSVReader reader = new CSVReaderBuilder(new FileReader(inputFilePath)).withSkipLines(1).build()) {
            List<String[]> rows = reader.readAll();

            for (String[] row : rows) {
                String userId = row[0];
                String firstName = row[1];
                String lastName = row[2];
                int version = Integer.parseInt(row[3]);
                String insuranceCompany = row[4];

                // Check if there is an existing record with the same UserId
                Optional<Map<String, String>> existingRecord = recordsByCompany
                        .getOrDefault(insuranceCompany, new ArrayList<>())
                        .stream()
                        .filter(record -> record.get("UserId").equals(userId))
                        .findFirst();

                if (existingRecord.isPresent()) {
                    // If version is higher, replace the existing record
                    if (version > Integer.parseInt(existingRecord.get().get("Version"))) {
                        recordsByCompany.get(insuranceCompany).remove(existingRecord.get());
                        recordsByCompany.get(insuranceCompany).add(getEnrolleeMap(userId, firstName, lastName, version, insuranceCompany));
                    }
                } else {
                    recordsByCompany.computeIfAbsent(insuranceCompany, k -> new ArrayList<>())
                            .add(getEnrolleeMap(userId, firstName, lastName, version, insuranceCompany));
                }
            }

            // Sort and write records to separate files for each Insurance Company
            for (Map.Entry<String, List<Map<String, String>>> entry : recordsByCompany.entrySet()) {
                List<Map<String, String>> sortedRecords = entry.getValue();
                sortedRecords.sort(Comparator.comparing(record -> record.get("LastName")));

                String outputFilePath = entry.getKey() + "_output.csv";
                try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(outputFilePath)).build()) {             
                	writer.writeNext(new String[]{"UserId", "FirstName", "LastName", "Version", "InsuranceCompany"});
                    for (Map<String, String> record : sortedRecords) {
                        writer.writeNext(new String[]{record.get("UserId"), record.get("FirstName"),
                                record.get("LastName"), record.get("Version"), record.get("InsuranceCompany")});
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, String> getEnrolleeMap(String userId, String firstName, String lastName, int version, String insuranceCompany) {
        Map<String, String> enrolleeMap = new HashMap<>();
        enrolleeMap.put("UserId", userId);
        enrolleeMap.put("FirstName", firstName);
        enrolleeMap.put("LastName", lastName);
        enrolleeMap.put("Version", String.valueOf(version));
        enrolleeMap.put("InsuranceCompany", insuranceCompany);
        return enrolleeMap;
    }
}
