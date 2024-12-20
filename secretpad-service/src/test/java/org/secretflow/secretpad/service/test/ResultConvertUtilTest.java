package org.secretflow.secretpad.service.test;

import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.service.model.report.ScqlReport;
import org.secretflow.secretpad.service.util.ResultConvertUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class ResultConvertUtilTest {


    @Test
    public void  testConvertScqlToSfReport_V2(){
        String ss = "{\"affected_rows\":\"0\",\"warnings\":[],\"cost_time_s\":7.305735944,\"out_columns\":[{\"name\":\"Column#4\",\"shape\":{\"dim\":[{\"dim_value\":\"1\"},{\"dim_value\":\"1\"}]},\"elem_type\":\"FLOAT32\",\"option\":\"VALUE\",\"annotation\":{\"status\":\"TENSORSTATUS_UNKNOWN\"},\"int32_data\":[],\"int64_data\":[],\"float_data\":[4.581761],\"double_data\":[],\"bool_data\":[],\"string_data\":[],\"data_validity\":[],\"ref_num\":0}]}";
        ScqlReport scqlReport = JsonUtils.toJavaObject(ss, ScqlReport.class);
        String content = ResultConvertUtil.convertScqlToSfReport(scqlReport);
        Assertions.assertNotNull(content);
    }



}
