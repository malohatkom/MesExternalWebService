/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package u_shiftorders;

import de.mpdv.customization.userExit.IUserExitParam;
import de.mpdv.sdi.data.DataTableColumnInfo;
import de.mpdv.sdi.data.SpecialParam;
import de.mpdv.sdi.data.ue.SdiAugmentSqlParam;
import de.mpdv.sdi.data.ue.SdiModifyResultListParam;
import de.mpdv.sdi.data.ue.SdiModifyResultListResult;
import de.mpdv.sdi.systemutility.IDataTable;
import de.mpdv.sdi.systemutility.ISdiLogger;
import de.mpdv.sdi.systemutility.ISdiLoggerProvider;
import de.mpdv.sdi.systemutility.ISystemUtilFactory;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author mikhail.malokhatko
 */
public class UShiftordersList {

    private final boolean printDebug = true;
    
    private static void write(String text) {
        String fileName = "C:\\Windows\\Temp\\UShiftordersList.log";
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
            out.println(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date()) + " " + text);
        } catch (IOException e) {
            System.err.println(e);
        } finally {
            if (out != null) {
                out.close();
            }
        }         
    }    
    
    public void sdiModifyResultList(IUserExitParam ueParam) {
    
        if (printDebug) write("UShiftordersList sdiModifyResultList");
        
        SdiModifyResultListParam param = (SdiModifyResultListParam)ueParam.get("param");
        Set<String> colSet = new LinkedHashSet<String>(param.getColumnConfigurator().getRequestedCols());
        IDataTable table = (param.getDataTables().size() > 0) ? (IDataTable)param.getDataTables().get(0) : null;
    
        if (table != null && table.getData() != null && table.getData().size() > 0) 
        {
            ISystemUtilFactory factory = (ISystemUtilFactory)ueParam.get("factory");
            ISdiLoggerProvider loggerProvider = (ISdiLoggerProvider)factory.fetchUtil("LoggerProvider");
            ISdiLogger logger = loggerProvider.fetchLogger(UShiftordersList.class);
      
            List<List<Object>> tableData = table.getData();
            Map<String, DataTableColumnInfo> metaMap = table.getMetadata();
            int departmentColIdx    = !colSet.contains("shiftorder.department") ? -1 : ((DataTableColumnInfo)metaMap.get("shiftorder.department")).getIndex();
            int shiftColIdx         = !colSet.contains("shiftorder.shift")      ? -1 : ((DataTableColumnInfo)metaMap.get("shiftorder.shift")).getIndex();
            int statusColIdx        = !colSet.contains("shiftorder.status")     ? -1 : ((DataTableColumnInfo)metaMap.get("shiftorder.status")).getIndex();
            int keyColIdx           = !colSet.contains("shiftorder.key")        ? -1 : ((DataTableColumnInfo)metaMap.get("shiftorder.key")).getIndex();
      
            Map<String, String> StatusText = new HashMap<String, String>();
            StatusText.put("C", "Создан");
            StatusText.put("R", "Выпущен");
            StatusText.put("M", "Снят");

            Map<String, String> DepartmentText = new HashMap<String, String>();
            DepartmentText.put("1", "ПК1");
            DepartmentText.put("2", "ПК2");        

            Map<String, String> ShiftText = new HashMap<String, String>();
            ShiftText.put("A", "День");
            ShiftText.put("B", "Ночь");
            
            List<String> keys = new ArrayList<String>();
            
            for (int i = 0; i < tableData.size(); i++) 
            {
                List<Object> row = tableData.get(i);
                if (departmentColIdx != -1 && DepartmentText.containsKey(row.get(departmentColIdx).toString())) row.set(departmentColIdx, DepartmentText.get(row.get(departmentColIdx).toString()));
                if (statusColIdx != -1 && StatusText.containsKey(row.get(statusColIdx).toString())) row.set(statusColIdx, StatusText.get(row.get(statusColIdx).toString()));
                if (shiftColIdx != -1 && ShiftText.containsKey(row.get(shiftColIdx).toString())) row.set(shiftColIdx, ShiftText.get(row.get(shiftColIdx).toString()));
            }
        } 
        ueParam.set("result", new SdiModifyResultListResult(table));
    } 
    
    
    public void sdiAugmentSql(IUserExitParam ueParam)
    {
        if (printDebug) write("UShiftordersList sdiModifyResultList");
        SdiAugmentSqlParam param = (SdiAugmentSqlParam)ueParam.get("param");
        if (printDebug) write("Select " + param.getSelect() + " From " + param.getFrom() + " Where " + param.getWhere() + " Order By " + param.getOrderBy());
    }
}