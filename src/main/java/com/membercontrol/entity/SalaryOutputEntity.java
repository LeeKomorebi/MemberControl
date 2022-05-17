package com.membercontrol.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalaryOutputEntity extends StaffInfoEntity {
	/** 残業時間 */
	private double overTimeHours;
	/** 残業代 */
	private double overtimeMoney;
	/** 源泉徴収税 */
	private double sourceTax;
	/** 労働保険 */
	private double labourInsurance;
	/** 通勤手当 */
	private double transportCosts;
	/** 厚生年金保険 */
	private double welfarePension;
	/** 合計G(課税) */
	private double taxSum;
	/** 支給額 */
	private double allowance;

	public SalaryOutputEntity() {
		super();
	}

	public SalaryOutputEntity(StaffInfoEntity info) {
		super(info.getId(), info.getName(), info.getInsuranceJoin(), info.getBasicSalary(), info.getPayDay(), info.getWorkInfo());
	}
}
