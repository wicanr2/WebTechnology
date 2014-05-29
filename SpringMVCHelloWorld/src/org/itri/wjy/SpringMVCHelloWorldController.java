package org.itri.wjy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.apache.commons.dbcp.BasicDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;


@Controller
public class SpringMVCHelloWorldController {
	
	
	@RequestMapping( method = {RequestMethod.GET, RequestMethod.POST}, value="/")
	@ResponseBody
	public String printHelloWorld(Model model, @RequestParam(value="whom",defaultValue="anr") String whom) {
		
		return "helloworld" + whom;
	}
	
	@RequestMapping(value = "/test", method = RequestMethod.GET)
	public String redirectToTestHtml() {

		return "redirect:/html/test.html";
	}
	
	@Autowired
	@Qualifier("dataSource")
	private DriverManagerDataSource dataSource;
	
	@Autowired
	@Qualifier("dataSource2")
	private BasicDataSource dataSource2;
	
	@RequestMapping( method = {RequestMethod.GET, RequestMethod.POST}, value="/q"
			,produces = "application/json"
			)
	@ResponseBody
	public String methodQ(Model model, 
			@RequestParam(value="q",defaultValue="select * from new_table") String q
			) {
		final JsonNodeFactory factory = JsonNodeFactory.instance;
		ObjectNode jsonObj = new ObjectNode(factory);
		ArrayNode arrayObj = new ArrayNode(factory);
		List<String> headerList = new ArrayList<String>();
		
		jsonObj.put("query",q);
		jsonObj.put("ret", 1);
		jsonObj.put("datasource", dataSource.toString());
		jsonObj.put("datasource2", dataSource2.toString());
		arrayObj.add(jsonObj);
		try {
			Connection conn =  dataSource2.getConnection();
			jsonObj.put("datasource2.conn", conn.toString());
			jsonObj.put("datasource2.initialSize", dataSource2.getInitialSize());
			PreparedStatement ps = conn.prepareStatement(q);
			ResultSet rs = ps.executeQuery(q);
			ResultSetMetaData rsmd = rs.getMetaData();
			for ( int i = 1 ; i <= rsmd.getColumnCount(); i++ ) {
				jsonObj.put("column"+i,rsmd.getColumnName(i));
				headerList.add(rsmd.getColumnName(i));
			}
			while (rs.next()) {
				ObjectNode j2 = new ObjectNode(factory);
				for ( int i = 1; i <= headerList.size(); i++ ) {
					j2.put(headerList.get(i-1), rs.getString(i));
				}
				arrayObj.add(j2);
			}
			rs.close();
			ps.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			jsonObj.put("exception",e.toString());
		}
		
		return arrayObj.toString();
	}
}
