package com.membercontrol.entity;

import lombok.Data;

@Data
public class WorkingInfoEntity {
	/** 労働時間実績 */
	private Double actualWorkingHours;
	/** 契約時間下限 */
	private Double minHour;
	/** 契約時間上限 */
	private Double maxHour;
}
