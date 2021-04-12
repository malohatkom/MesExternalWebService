/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mpdv.SimpleExternalService;

import de.mpdv.sdi.data.DataType;
import de.mpdv.sdi.data.SesContext;
import de.mpdv.sdi.data.SesRequest;
import de.mpdv.sdi.data.SesResult;
import de.mpdv.sdi.data.SesResultBuilder;
import de.mpdv.sdi.simpleExternalService.ISimpleExternalService;
import de.mpdv.sdi.systemutility.IDataTableBuilder;
import de.mpdv.sdi.systemutility.ISystemUtilFactory;

/**
 *
 * @author Михаил
 */
public class U_JavaTesto implements ISimpleExternalService {
    
    public SesResult execute(SesRequest request, SesContext context, ISystemUtilFactory factory)
    {
    	IDataTableBuilder builder = factory.fetchUtil("DataTableBuilder");
        builder.addCol("testo.purum", DataType.STRING);
	builder.addCol("testo.param", DataType.STRING);
        
        builder.addRow();
        builder.value("turum");
        builder.value("purum");
	return new SesResultBuilder().addDataTable(builder.build()).build();
    }
}
