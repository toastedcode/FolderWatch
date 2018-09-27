package com.toast.foldlerwatch.parser;

import java.io.File;
import java.io.IOException;

import com.toast.foldlerwatch.oasisreport.OasisReport;

public interface Parser
{
   public void parse(File file, OasisReport report) throws IOException, ParseException;
}
