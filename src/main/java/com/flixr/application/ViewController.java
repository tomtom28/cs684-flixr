/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flixr.application;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
public class ViewController {

	// Renders the index.html page for ReactJS
	@RequestMapping(value = "/")
	public String index() {
		return "index";
	}

	// TODO - Fake Demo
	@GetMapping("/api/employees")
	@ResponseBody
	public HashMap<String,List<HashMap<String,String>>> getEmployees() {

		// TODO - Just a sample API response
		HashMap<String, List<HashMap<String,String>> > sampleJSON = new HashMap<>();

		HashMap<String, String> employee1 = new HashMap<>();
		employee1.put("employeeId", "101");
		employee1.put("firstName", "Tony");
		employee1.put("lastName", "Tiger");
		employee1.put("description", "He was a Tiger who liked cereal.");


		List<HashMap<String, String>> listEmployees = new ArrayList<>();
		listEmployees.add(employee1);

		sampleJSON.put("employees", listEmployees);

		return sampleJSON;
	}
	


}