package com.membercontrol.service;

import com.membercontrol.entity.SalaryOutputEntity;
import com.membercontrol.entity.StaffInfoEntity;
import com.membercontrol.entity.WorkingInfoEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedList;
import java.util.List;

@Service
public class CalculateService {
    /**
     * CSV文件地址
     */
    @Value("${staff.info.csv.url}")
    private String CSV_INPUT_PATH;

    @Value("${csv.output}")
    private String CSV_OUTPUT_PATH;

    /**
     * CSV文件名
     */
    @Value("${staff.info.csv.name}")
    private String CSV_NAME;

    /**
     * 給料明細を出力する
     */
    public void paySlipsOutput() throws IOException {
        // 1.读取CSV
        List<StaffInfoEntity> staffs;
        try {
            staffs = this.readCSV();
        } catch (IOException | RuntimeException e) {
            throw new RuntimeException(e);
        }

        // 2.计算
        List<SalaryOutputEntity> salaries;
        try {
            salaries = this.calculateSalary(staffs);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

        // 3.输出
        salaries.forEach(s -> {
            try {
                createCsv(s);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


    /**
     * 读取CSV
     *
     * @return 员工信息
     */
    List<StaffInfoEntity> readCSV() throws IOException, RuntimeException {
        File csv = new File(CSV_INPUT_PATH + CSV_NAME);
        BufferedReader reader = new BufferedReader(new FileReader(csv));

        List<StaffInfoEntity> result = new LinkedList<>();
        List<String> errorList = new LinkedList<>();

        String line;
        int lineNum = 1;
        while ((line = reader.readLine()) != null) {
            if (lineNum > 1) {
                String[] data = line.split(",");
                StringBuilder error = new StringBuilder();

                StaffInfoEntity staff = new StaffInfoEntity();
                WorkingInfoEntity workingInfo = new WorkingInfoEntity();
                // 社員番号
                try {
                    staff.setId(this.checkId(data[0]));
                } catch (InvalidObjectException e) {
                    error.append(lineNum).append(e.getMessage());
                    error.append(System.lineSeparator());
                }
                // 社員名
                try {
                    staff.setName(this.checkName(data[1]));
                } catch (InvalidObjectException e) {
                    error.append(lineNum).append(e.getMessage());
                    error.append(System.lineSeparator());
                }
                // 年金情報
                try {
                    staff.setInsuranceJoin(this.checkInsurance(data[2]));
                } catch (InvalidObjectException e) {
                    error.append(lineNum).append(e.getMessage());
                    error.append(System.lineSeparator());
                }
                // 基本給
                try {
                    staff.setBasicSalary(this.checkBasicSalary(data[3]));
                } catch (InvalidObjectException e) {
                    error.append(lineNum).append(e.getMessage());
                    error.append(System.lineSeparator());
                }
                // 労働時間
                try {
                    workingInfo.setActualWorkingHours(this.checkActualWorkingHours(data[4]));
                } catch (InvalidObjectException e) {
                    error.append(lineNum).append(e.getMessage());
                    error.append(System.lineSeparator());
                }
                // 契約時間下限
                try {
                    workingInfo.setMinHour(this.checkMinHour(data[5]));
                } catch (InvalidObjectException e) {
                    error.append(lineNum).append(e.getMessage());
                    error.append(System.lineSeparator());
                }
                // 契約時間上限
                try {
                    workingInfo.setMaxHour(this.checkMaxHour(data[6], workingInfo.getMinHour()));
                } catch (InvalidObjectException e) {
                    error.append(lineNum).append(e.getMessage());
                    error.append(System.lineSeparator());
                }
                // 給料日
                try {
                    staff.setPayDay(this.checkPayDay(data[7]));
                } catch (InvalidObjectException e) {
                    error.append(lineNum).append(e.getMessage());
                }
                if (!error.isEmpty()) {
                    errorList.add(error.toString());
                    continue;
                }
                staff.setWorkInfo(workingInfo);

                result.add(staff);
            }
            lineNum++;
        }
        reader.close();

        if (!errorList.isEmpty()) {
            throw new RuntimeException(String.join(System.lineSeparator(), errorList));
        }

        return result;
    }

    /**
     * 计算工资
     *
     * @param staffInfoEntityList 员工List
     * @return 员工工资List
     */
    List<SalaryOutputEntity> calculateSalary(List<StaffInfoEntity> staffInfoEntityList) throws RuntimeException {
        try {
/*			List<SalaryOutputEntity> result = new ArrayList<>();
			for (StaffInfoEntity el : staffInfoEntityList) {
				var salary = new SalaryOutputEntity(el);

				// 残業/控除が発生
				if (!el.getWorkInfo().getActualWorkingHours().equals(el.getWorkInfo().getMaxHour())) {
					// 残業代単価
					int perHour = Math.toIntExact(Math.round(el.getBasicSalary() / el.getWorkInfo().getMaxHour()));
					// 残業/控除の時間設定
					salary.setOverTimeHours(el.getWorkInfo().getActualWorkingHours() - el.getWorkInfo().getMaxHour());
					// 残業/控除の金額計算
					salary.setOvertimeMoney(salary.getOverTimeHours() * perHour);
				}

				// 合計Gの計算
				salary.setTaxSum(el.getBasicSalary() + salary.getOvertimeMoney());
				// 源泉徴収の計算
				salary.setSourceTax(salary.getTaxSum() * 0.02);
				// 労働保険の計算
				salary.setLabourInsurance((salary.getTaxSum() + salary.getTransportCosts()) * 0.03);
				// 厚生年金の計算
				if (el.getInsuranceJoin()) {
					salary.setWelfarePension((salary.getTaxSum() + salary.getTransportCosts()) * 0.15);
				}
				// 支給額
				salary.setAllowance(salary.getTaxSum() - salary.getSourceTax() - salary.getLabourInsurance() - salary.getWelfarePension() + salary.getTransportCosts());
				result.add(salary);
			}
			return result;*/
            return staffInfoEntityList.stream().map(el -> {
                var salary = new SalaryOutputEntity(el);

                // 残業/控除が発生
                if (!el.getWorkInfo().getActualWorkingHours().equals(el.getWorkInfo().getMaxHour())) {
                    // 残業代単価
                    int perHour = Math.toIntExact(Math.round(el.getBasicSalary() / el.getWorkInfo().getMaxHour()));
                    // 残業/控除の時間設定
                    salary.setOverTimeHours(el.getWorkInfo().getActualWorkingHours() - el.getWorkInfo().getMaxHour());
                    // 残業/控除の金額計算
                    salary.setOvertimeMoney(salary.getOverTimeHours() * perHour);
                }

                // 合計Gの計算
                salary.setTaxSum(el.getBasicSalary() + salary.getOvertimeMoney());
                // 源泉徴収の計算
                salary.setSourceTax(salary.getTaxSum() * 0.02);
                // 労働保険の計算
                salary.setLabourInsurance((salary.getTaxSum() + salary.getTransportCosts()) * 0.03);
                // 厚生年金の計算
                if (el.getInsuranceJoin()) {
                    salary.setWelfarePension((salary.getTaxSum() + salary.getTransportCosts()) * 0.15);
                }
                // 支給額
                salary.setAllowance(salary.getTaxSum() - salary.getSourceTax() - salary.getLabourInsurance() - salary.getWelfarePension() + salary.getTransportCosts());
                return salary;
            }).toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 输出为CSV文件
     *
     * @param outputEntity 员工工资情報
     */
    void createCsv(SalaryOutputEntity outputEntity) throws IOException {
        StringBuilder fileName = new StringBuilder();
        fileName.append(outputEntity.getId()).append(outputEntity.getName()).append("様　給料明細書");
        /* ↑ = String fileNameStr = outputEntity.getId() + "" + outputEntity.getName() + "様　給料明細書"; */

        File csvFile;
        BufferedWriter writer = null;
        try {
            csvFile = new File(CSV_OUTPUT_PATH + File.separator + fileName);
            File parent = csvFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            if (csvFile.createNewFile()) {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), StandardCharsets.UTF_8), 1024);

                // 标题
                String[] header = new String[]{"社員番号", "氏名", "年金加入", "基本給", "労働時間", "契約時間下限", "契約時間上限", "残業時間", "残業代", "源泉徴収税", "労働保険", "通勤手当", "厚生年金保険", "合計G(課税) ", "支給額", "支払日"};

                // 文件名写在第一行
                StringBuilder builder = new StringBuilder();
                builder.append(" ".repeat(header.length / 2));
                writer.write(builder.toString() + fileName + builder);
                writer.newLine();

                // 标题行
                writer.write(String.join(",", header));
                writer.newLine();

                // 内容
                writer.write(outputEntity.getId());
                writer.write(",");
                writer.write(outputEntity.getName());
                writer.write(",");
                writer.write(outputEntity.getInsuranceJoin() ? "○" : "加入しない");
                writer.write(",");
                writer.write(outputEntity.getBasicSalary().intValue());
                writer.write(",");
                writer.write(outputEntity.getWorkInfo().getActualWorkingHours().intValue());
                writer.write(",");
                writer.write(outputEntity.getWorkInfo().getMinHour().intValue());
                writer.write(",");
                writer.write(outputEntity.getWorkInfo().getMaxHour().intValue());
                writer.write(",");
                writer.write((int) outputEntity.getOverTimeHours());
                writer.write(",");
                writer.write((int) outputEntity.getOvertimeMoney());
                writer.write(",");
                writer.write((int) outputEntity.getSourceTax());
                writer.write(",");
                writer.write((int) outputEntity.getLabourInsurance());
                writer.write(",");
                writer.write((int) outputEntity.getTransportCosts());
                writer.write(",");
                writer.write((int) outputEntity.getWelfarePension());
                writer.write(",");
                writer.write((int) outputEntity.getTaxSum());
                writer.write(",");
                writer.write((int) outputEntity.getAllowance());
                writer.write(outputEntity.getPayDay());
                writer.newLine();

                writer.flush();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    // region Private function
    private String checkId(String datum) throws InvalidObjectException {
        if (datum.length() == 5) {
            return datum;
        } else {
            throw new InvalidObjectException("行目データのCSV作成失敗しました、社員番号のデータを再確認してください。");
        }
    }

    private String checkPayDay(String datum) throws InvalidObjectException {
        try {
            return LocalDate.parse(datum).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        } catch (DateTimeParseException e) {
            throw new InvalidObjectException("行目データのCSV作成失敗しました、給料日のデータを再確認してください。");
        }
    }

    private Double checkMaxHour(String datum, Double minHour) throws InvalidObjectException {
        try {
            var maxHour = Double.parseDouble(datum);
            if (maxHour > minHour) return maxHour;
            else throw new InvalidObjectException("行目データのCSV作成失敗しました、契約時間上限のデータを再確認してください。");
        } catch (NumberFormatException e) {
            throw new InvalidObjectException("行目データのCSV作成失敗しました、契約時間上限のデータを再確認してください。");
        }
    }

    private Double checkMinHour(String datum) throws InvalidObjectException {
        try {
            return Double.parseDouble(datum);
        } catch (NumberFormatException e) {
            throw new InvalidObjectException("行目データのCSV作成失敗しました、契約時間下限のデータを再確認してください。");
        }
    }

    private Double checkActualWorkingHours(String datum) throws InvalidObjectException {
        try {
            return Double.parseDouble(datum);
        } catch (NumberFormatException e) {
            throw new InvalidObjectException("行目データのCSV作成失敗しました、労働時間のデータを再確認してください。");
        }
    }

    private Double checkBasicSalary(String datum) throws InvalidObjectException {
        try {
            return Double.parseDouble(datum);
        } catch (NumberFormatException e) {
            throw new InvalidObjectException("行目データのCSV作成失敗しました、基本給のデータを再確認してください。");
        }
    }

    private Boolean checkInsurance(String datum) throws InvalidObjectException {
        if (StringUtils.hasText(datum)) {
            return datum.equals("○");
        } else {
            throw new InvalidObjectException("行目データのCSV作成失敗しました、年金加入状況のデータを再確認してください。");
        }
    }

    private String checkName(String datum) throws InvalidObjectException {
        if (StringUtils.hasText(datum)) {
            return datum;
        } else {
            throw new InvalidObjectException("行目データのCSV作成失敗しました、姓名のデータを再確認してください。");
        }
    }
    // endregion
}
