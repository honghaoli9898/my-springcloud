package com.seaboxdata.sdps.common.framework.bean.ambari;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import cn.hutool.json.JSONArray;

import com.alibaba.fastjson.JSONObject;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AmbariUser implements Serializable {
	private static final long serialVersionUID = 260727366266615275L;

	private String user_name;

	private String password;

	private Boolean active;

	private Boolean admin;

	private String permission_name;
	
	private String old_password;
	
	public JSONObject getRequestUpdatePasswdBody() {
		JSONObject body = new JSONObject();
		body.put("Users/old_password",this.old_password);
		body.put("Users/password", this.password);
		return body;
	}

	public JSONObject getRequestCreateUserBody() {
		JSONObject body = new JSONObject();
		body.put("Users/user_name", this.user_name);
		body.put("Users/password", this.password);
		body.put("Users/active", this.active);
		body.put("Users/admin", this.admin);
		return body;
	}

	public JSONArray getRequestCreatePrivilegesBody() {
		JSONArray array = new JSONArray();
		JSONObject data = new JSONObject();
		data.put("permission_name", this.permission_name);
		data.put("principal_name", this.user_name);
		data.put("principal_type", "USER");
		JSONObject body = new JSONObject();
		body.put("PrivilegeInfo", data);
		array.put(body);
		return array;
	}

}
