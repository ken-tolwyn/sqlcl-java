package io.kenbugs.sqlcldemo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import oracle.dbtools.extension.SQLCLService;
import oracle.dbtools.raptor.newscriptrunner.CommandListener;
import oracle.dbtools.raptor.newscriptrunner.IHelp;
import oracle.dbtools.raptor.newscriptrunner.ISQLCommand;
import oracle.dbtools.raptor.newscriptrunner.ScriptRunnerContext;
import oracle.dbtools.raptor.query.Bind;
import oracle.dbtools.raptor.query.Parser;
import oracle.jdbc.OracleCallableStatement;

public class DemoCmd extends CommandListener implements IHelp, SQLCLService {
    public DemoCmd() {
    }

    @Override
    public Class<? extends CommandListener> getCommandListener() {
        return DemoCmd.class;
    }

    @Override
    public String getExtensionDescription() {
        return "Desciption";
    }

    @Override
    public String getExtensionName() {
        return "democmd";
    }

    @Override
    public String getExtensionVersion() {
        return "1.0";
    }

    @Override
    public String getCommand() {
        return "democmd";
    }

    @Override
    public String getHelp() {
        return "runs awesome tests";
    }

    @Override
    public boolean isSqlPlus() {
        return false;
    }

    @Override
    public boolean handleEvent(Connection conn, ScriptRunnerContext ctx, ISQLCommand cmd) {
        if (matches(getCommand(),cmd.getSql())) {
        // out variables variable_1 = 'A' varaible_2 = 'AC'
        // in variables variable_3 = 'C'
        String plsqlBlock = "begin :variable_1 := 'A'; :variable_2 := :variable_3; end;";
        Map<String, String> inBinds = new HashMap<>();
        ArrayList<String> outBinds = new ArrayList<>();
        
        outBinds.add("variable_1");
        outBinds.add("variable_2");
        inBinds.put("variable_3", "C");
        
        Map<String, String> outputBinds = new HashMap<>();
        try {
            OracleCallableStatement prepare = (OracleCallableStatement) conn.prepareCall(plsqlBlock);

            // this call will generate the binds that it can find in the plsql block
            ArrayList<Bind> binds = Parser.getInstance().getBinds(plsqlBlock, true);
        // iterate over the binds found in the PLSQL block
        ctx.writeln("Showing all binds found");
        for (Bind bind : binds) {
            ctx.writeln("Bind: " + bind.getName());
        }
            
            int i = 0;

            // iterate over the binds found in the PLSQL block
            // compare the names with the names provided in the input binds and the output binds
            while (i < binds.size()) {
                    if(inBinds.containsKey(binds.get(i).getName())){
                        prepare.setString(binds.get(i).getName(), inBinds.get(binds.get(i).getName()));
                    } else {
                        if (outBinds.contains(binds.get(i).getName())) {
                            // register the varialbe as on output variable with the type of string (sql code 12)
                            prepare.registerOutParameter(binds.get(i).getName(), 12);

                        }
                    }
                    i++;
            }
            
            //execute the query

             prepare.executeUpdate();
             
             // retrieve the output binds variables
                for (String entry : outBinds) {
                outputBinds.put(entry, prepare.getString(entry));
            }

            //iterate over the outputBinds
            for (Map.Entry<String, String> entry : outputBinds.entrySet()) {
                ctx.writeln(entry.getKey() + " = " + entry.getValue());                
            }
            prepare.close();
            
        }
        catch(SQLException e){
            ctx.write( "Error: " + e.getMessage());
        }
        return true;
    }
    return true;
    }

    @Override
    public void beginEvent(Connection conn, ScriptRunnerContext ctx, ISQLCommand cmd) {
    }

    @Override
    public void endEvent(Connection conn, ScriptRunnerContext ctx, ISQLCommand cmd) {
    }
}
