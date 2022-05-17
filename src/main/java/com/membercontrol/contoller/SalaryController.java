package com.membercontrol.contoller;

import com.membercontrol.service.CalculateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class SalaryController {
	private final CalculateService calculateService;

	@Autowired
	public SalaryController(CalculateService calculateService) {
		this.calculateService = calculateService;
	}


	/**
	 * 給料明細を出力
	 *
	 * @return 状態 success/fail
	 */
	@RequestMapping(path = "/calculate", method = RequestMethod.POST)
	public ResponseEntity<String> calculatePaying() {
		try {
			calculateService.paySlipsOutput();
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
