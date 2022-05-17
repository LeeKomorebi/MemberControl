package com.membercontrol.entity;

import lombok.Data;

@Data
public class StaffInfoEntity {
	/** 社員番号 */
	private String id;
	/** 社員名 */
	private String name;
	/** 年金加入 */
	private Boolean insuranceJoin;
	/** 基本給 */
	private Double basicSalary;
	/** 給料日 */
	private String payDay;
	/** 労働情報 */
	private WorkingInfoEntity workInfo;

	public StaffInfoEntity() {
	}

	public StaffInfoEntity(String id, String name, Boolean insuranceJoin, Double basicSalary, String payDay, WorkingInfoEntity workInfo) {
		this.id = id;
		this.name = name;
		this.insuranceJoin = insuranceJoin;
		this.basicSalary = basicSalary;
		this.payDay = payDay;
		this.workInfo = workInfo;
	}
}
