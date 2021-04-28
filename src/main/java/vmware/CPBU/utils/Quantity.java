package vmware.CPBU.utils;

import vmware.CPBU.exceptions.VMOperatorException;

public class Quantity {
    public static long getLong(String quantity) throws VMOperatorException {
        String lowcase = quantity.trim().toLowerCase();
        String digitals="";
        long result=0;
        //100m,100Mi,100Mib
        if (lowcase.endsWith("m") || lowcase.endsWith("mi") || lowcase.endsWith("mib")) {
            digitals = lowcase.substring(0,lowcase.indexOf("m"));
        }

        //1G,2Gi,2Gib
        if (lowcase.endsWith("g") || lowcase.endsWith("gi") || lowcase.endsWith("gib")) {
            digitals = lowcase.substring(0,lowcase.indexOf("g"))+"000";
        }
        try {
            result = Long.parseLong(digitals);
        } catch(NumberFormatException e) {
            throw new VMOperatorException(VMOperatorException.ExceptionCode.QUANTITY_PARSE_ERROR);
        }
        return result;
    }
}
