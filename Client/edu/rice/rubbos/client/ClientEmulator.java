package edu.rice.rubbos.client;

import edu.rice.rubbos.beans.TimeManagement;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.Runtime;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.Vector;

/**
 * RUBBoS client emulator. 
 * This class plays random user sessions emulating a Web browser.
 *
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.0
 */
public class ClientEmulator
{
  private RUBBoSProperties rubbos = null;        // access to rubbos.properties file
  private URLGenerator    urlGen = null;        // URL generator corresponding to the version to be used (PHP, EJB or Servlets)
  private static float    slowdownFactor = 0;
  private static boolean  endOfSimulation = false;

  /**
   * Creates a new <code>ClientEmulator</code> instance.
   * The program is stopped on any error reading the configuration files.
   */
  public ClientEmulator()
  {
    // Initialization, check that all files are ok
    rubbos = new RUBBoSProperties();
    urlGen = rubbos.checkPropertiesFileAndGetURLGenerator();
    if (urlGen == null)
      Runtime.getRuntime().exit(1);
    // Check that the transition table is ok and print it
    TransitionTable transition = new TransitionTable(rubbos.getNbOfColumns(), rubbos.getNbOfRows(), null, rubbos.useTPCWThinkTime());
    if (!transition.ReadExcelTextFile(rubbos.getUserTransitionTable()))
      Runtime.getRuntime().exit(1);
    else
      transition.displayMatrix("User");
    if (!transition.ReadExcelTextFile(rubbos.getAuthorTransitionTable()))
      Runtime.getRuntime().exit(1);
    else
      transition.displayMatrix("Author");
  }


  /**
   * Updates the slowdown factor.
   *
   * @param newValue new slowdown value
   */
  private synchronized void setSlowDownFactor(float newValue)
  {
    slowdownFactor = newValue;
  }


  /**
   * Get the slowdown factor corresponding to current ramp (up, session or down).
   */
  public static synchronized float getSlowDownFactor()
  {
    return slowdownFactor;
  }


  /**
   * Set the end of the current simulation
   */
  private synchronized void setEndOfSimulation()
  {
    endOfSimulation = true;
  }


  /**
   * True if end of simulation has been reached.
   */
  public static synchronized boolean isEndOfSimulation()
  {
    return endOfSimulation;
  }


  /**
   * Main program take an optional output file argument only 
   * if it is run on as a remote client.
   *
   * @param args optional output file if run as remote client
   */
  public static void main(String[] args)
  {
    GregorianCalendar startDate;
    GregorianCalendar endDate;
    GregorianCalendar upRampDate;
    GregorianCalendar runSessionDate;
    GregorianCalendar downRampDate;
    GregorianCalendar endDownRampDate;
    Process           webServerMonitor = null;
    Process           dbServerMonitor = null;
    Process           clientMonitor;
    Process[]         remoteClientMonitor = null;
    Process[]         remoteClient = null;
    String            reportDir = "";
    boolean           isMainClient = args.length == 0; // Check if we are the main client

    if (isMainClient)
    { 
      // Start by creating a report directory and redirecting output to an index.html file
      System.out.println("RUBBoS client emulator - (C) Rice University/INRIA 2001\n");
      reportDir = "bench/"+TimeManagement.currentDateToString()+"/";
      reportDir = reportDir.replace(' ', '@');
      try
      {
        System.out.println("Creating report directory "+reportDir);
        File dir = new File(reportDir);
        dir.mkdirs();
        if (!dir.isDirectory())
        {
          System.out.println("Unable to create "+reportDir+" using current directory instead");
          reportDir = "./";
        }
        else
          reportDir = dir.getCanonicalPath()+"/";
        System.out.println("Redirecting output to '"+reportDir+"index.html'");
        PrintStream outputStream = new PrintStream(new FileOutputStream(reportDir+"index.html"));
        System.out.println("Please wait while experiment is running ...");
        System.setOut(outputStream);
        System.setErr(outputStream);
      }
      catch (Exception e)
      {
        System.out.println("Output redirection failed, displaying results on standard output ("+e.getMessage()+")");
      }
      System.out.println("<h2>RUBBoS client emulator - (C) Rice University/INRIA 2001</h2><p>\n");
      startDate = new GregorianCalendar();
      System.out.println("<h3>Test date: "+TimeManagement.dateToString(startDate)+"</h3><br>\n");
    
      System.out.println("<A HREF=\"#config\">Test configuration</A><br>");
      System.out.println("<A HREF=\"trace_client0.html\">Test trace</A><br>");
      System.out.println("<A HREF=\"perf.html\">Test performance report</A><br><p>");
      System.out.println("<p><hr><p>");

      System.out.println("<CENTER><A NAME=\"config\"></A><h2>*** Test configuration ***</h2></CENTER>");
    }
    else
    {
      System.out.println("RUBBoS remote client emulator - (C) Rice University/INRIA 2001\n");
      startDate = new GregorianCalendar();
    }

    ClientEmulator client = new ClientEmulator(); // Get also rubbos.properties info

    Stats          stats = new Stats(client.rubbos.getNbOfRows());
    Stats          upRampStats = new Stats(client.rubbos.getNbOfRows());
    Stats          runSessionStats = new Stats(client.rubbos.getNbOfRows());
    Stats          downRampStats = new Stats(client.rubbos.getNbOfRows());
    Stats          allStats = new Stats(client.rubbos.getNbOfRows());
    UserSession[]  sessions = new UserSession[client.rubbos.getNbOfClients()];
    
    System.out.println("<p><hr><p>");

    if (isMainClient)
    {
      // Start remote clients
      System.out.println("Total number of clients for this experiment: "+(client.rubbos.getNbOfClients()*(client.rubbos.getRemoteClients().size()+1))+"<br>");
      remoteClient = new Process[client.rubbos.getRemoteClients().size()];
      for (int i = 0 ; i < client.rubbos.getRemoteClients().size() ; i++)
      {
        try
        {
          System.out.println("ClientEmulator: Starting remote client on "+client.rubbos.getRemoteClients().get(i)+"<br>\n");
          String[] rcmdClient = new String[3];
          rcmdClient[0] = client.rubbos.getMonitoringRsh();
          rcmdClient[1] = (String)client.rubbos.getRemoteClients().get(i);
          rcmdClient[2] = client.rubbos.getClientsRemoteCommand()+" "+reportDir+"trace_client"+(i+1)+".html "+reportDir+"stat_client"+(i+1)+".html";
          remoteClient[i] = Runtime.getRuntime().exec(rcmdClient);
          System.out.println("&nbsp &nbsp Command is: "+rcmdClient[0]+" "+rcmdClient[1]+" "+rcmdClient[2]+"<br>\n");
        }
        catch (IOException ioe)
        {
          System.out.println("An error occured while executing remote client ("+ioe.getMessage()+")");
        }
      }

      // Start monitoring programs
      System.out.println("<CENTER></A><A NAME=\"trace\"><h2>*** Monitoring ***</h2></CENTER>");
      try
      {
        // Monitor Web server
        int fullTimeInSec = (client.rubbos.getUpRampTime()+client.rubbos.getSessionTime()+client.rubbos.getDownRampTime())/1000 + 5; // Give 5 seconds extra for init
        System.out.println("ClientEmulator: Starting monitoring program on Web server "+client.rubbos.getWebServerName()+"<br>\n");
        String[] cmdWeb = new String[5];
        cmdWeb[0] = client.rubbos.getMonitoringRsh();
        cmdWeb[1] = client.rubbos.getWebServerName();                                               
        cmdWeb[2] = "/bin/bash";
        cmdWeb[3] = "-c";
        cmdWeb[4] = "'LANG=en_GB.UTF-8 "+client.rubbos.getMonitoringProgram()+" "+client.rubbos.getMonitoringOptions()+" "+
          client.rubbos.getMonitoringSampling()+" "+fullTimeInSec+" > "+reportDir+"web_server'";
        webServerMonitor = Runtime.getRuntime().exec(cmdWeb);
        System.out.println("&nbsp &nbsp Command is: "+cmdWeb[0]+" "+cmdWeb[1]+" "+cmdWeb[2]+" "+cmdWeb[3]+" "+cmdWeb[4]+"<br>\n");
      
        // Monitor Database server
        System.out.println("ClientEmulator: Starting monitoring program on Database server "+client.rubbos.getDBServerName()+"<br>\n");
        String[] cmdDB = new String[5];
        cmdDB[0] = client.rubbos.getMonitoringRsh();
        cmdDB[1] = client.rubbos.getDBServerName();
        cmdDB[2] = "/bin/bash";
        cmdDB[3] = "-c";
        cmdDB[4] = "'LANG=en_GB.UTF-8 "+client.rubbos.getMonitoringProgram()+" "+client.rubbos.getMonitoringOptions()+" "+
          client.rubbos.getMonitoringSampling()+" "+fullTimeInSec+" > "+reportDir+"db_server'";
        dbServerMonitor = Runtime.getRuntime().exec(cmdDB);
        System.out.println("&nbsp &nbsp Command is: "+cmdDB[0]+" "+cmdDB[1]+" "+cmdDB[2]+" "+cmdDB[3]+" "+cmdDB[4]+"<br>\n");

        // Monitor local client
        System.out.println("ClientEmulator: Starting monitoring program locally on client<br>\n");
        String[] cmdClient = new String[5];
        cmdClient[0] = client.rubbos.getMonitoringRsh();
        cmdClient[1] = "localhost";
        cmdClient[2] = "/bin/bash";
        cmdClient[3] = "-c";
        cmdClient[4] = "'LANG=en_GB.UTF-8 "+client.rubbos.getMonitoringProgram()+" "+client.rubbos.getMonitoringOptions()+" "+
          client.rubbos.getMonitoringSampling()+" "+fullTimeInSec+" > "+reportDir+"client0'";
        clientMonitor = Runtime.getRuntime().exec(cmdClient);
        System.out.println("&nbsp &nbsp Command is: "+cmdClient[0]+" "+cmdClient[1]+" "+cmdClient[2]+" "+cmdClient[3]+" "+cmdClient[4]+"<br>\n");

        remoteClientMonitor = new Process[client.rubbos.getRemoteClients().size()];
        // Monitor remote clients
        for (int i = 0 ; i < client.rubbos.getRemoteClients().size() ; i++)
        {
          System.out.println("ClientEmulator: Starting monitoring program locally on client<br>\n");
          String[] rcmdClient = new String[5];
          rcmdClient[0] = client.rubbos.getMonitoringRsh();
          rcmdClient[1] = (String)client.rubbos.getRemoteClients().get(i);
          rcmdClient[2] = "/bin/bash";
          rcmdClient[3] = "-c";
          rcmdClient[4] = "'LANG=en_GB.UTF-8 "+client.rubbos.getMonitoringProgram()+" "+client.rubbos.getMonitoringOptions()+" "+
            client.rubbos.getMonitoringSampling()+" "+fullTimeInSec+" > "+reportDir+"client"+(i+1)+"'";
          remoteClientMonitor[i] = Runtime.getRuntime().exec(rcmdClient);
          System.out.println("&nbsp &nbsp Command is: "+rcmdClient[0]+" "+rcmdClient[1]+" "+rcmdClient[2]+" "+rcmdClient[3]+" "+rcmdClient[4]+"<br>\n");
        }

        // Redirect output for traces
        PrintStream outputStream = new PrintStream(new FileOutputStream(reportDir+"trace_client0.html"));
        System.setOut(outputStream);
        System.setErr(outputStream);
      }
      catch (IOException ioe)
      {
        System.out.println("An error occured while executing monitoring program ("+ioe.getMessage()+")");
      }
    }
    else
    { // Redirect output of remote clients
      System.out.println("Redirecting output to '"+args[0]+"'");
      try
      {
        PrintStream outputStream = new PrintStream(new FileOutputStream(args[0]));
        System.out.println("Please wait while experiment is running ...");
        System.setOut(outputStream);
        System.setErr(outputStream);
      }
      catch (Exception e)
      {
        System.out.println("Output redirection failed, displaying results on standard output ("+e.getMessage()+")");
      }
      startDate = new GregorianCalendar();
    }


    // #############################
    // ### TEST TRACE BEGIN HERE ###
    // #############################

    System.out.println("<CENTER></A><A NAME=\"trace\"><h2>*** Test trace ***</h2></CENTER><p>");
    System.out.println("<A HREF=\"trace_client0.html\">Main client traces</A><br>");
    for (int i = 0 ; i < client.rubbos.getRemoteClients().size() ; i++)
      System.out.println("<A HREF=\"trace_client"+(i+1)+".html\">client1 ("+client.rubbos.getRemoteClients().get(i)+") traces</A><br>");
    System.out.println("<br><p>");
    System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#up\">Up ramp trace</A><br>");
    System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#run\">Runtime session trace</A><br>");
    System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#down\">Down ramp trace</A><br><p><p>");

    // Run user sessions
    System.out.println("ClientEmulator: Starting "+client.rubbos.getNbOfClients()+" session threads<br>");
    Random rand = new Random();   // random number generator
    for (int i = 0 ; i < client.rubbos.getNbOfClients() ; i++)
    {
      if (rand.nextInt(100) < client.rubbos.getPercentageOfAuthors())
        sessions[i] = new UserSession("AuthorSession"+i, client.urlGen, client.rubbos, stats, true);
      else
        sessions[i] = new UserSession("UserSession"+i, client.urlGen, client.rubbos, stats, false);
      sessions[i].start();
    }

    // Start up-ramp
    System.out.println("<br><A NAME=\"up\"></A>");
    System.out.println("<h3>ClientEmulator: Switching to ** UP RAMP **</h3><br><p>");
    client.setSlowDownFactor(client.rubbos.getUpRampSlowdown());
    upRampDate = new GregorianCalendar();
    try
    {
      Thread.currentThread().sleep(client.rubbos.getUpRampTime());
    }
    catch (java.lang.InterruptedException ie)
    {
      System.err.println("ClientEmulator has been interrupted.");
    }
    upRampStats.merge(stats);
    stats.reset(); // Note that as this is not atomic we may lose some stats here ...

    // Start runtime session
    System.out.println("<br><A NAME=\"run\"></A>");
    System.out.println("<h3>ClientEmulator: Switching to ** RUNTIME SESSION **</h3><br><p>");
    client.setSlowDownFactor(1);
    runSessionDate = new GregorianCalendar();
    try
    {
      Thread.currentThread().sleep(client.rubbos.getSessionTime());
    }
    catch (java.lang.InterruptedException ie)
    {
      System.err.println("ClientEmulator has been interrupted.");
    }
    runSessionStats.merge(stats);
    stats.reset(); // Note that as this is not atomic we may lose some stats here ...

    // Start down-ramp
    System.out.println("<br><A NAME=\"down\"></A>");
    System.out.println("<h3>ClientEmulator: Switching to ** DOWN RAMP **</h3><br><p>");
    client.setSlowDownFactor(client.rubbos.getDownRampSlowdown());
    downRampDate = new GregorianCalendar();
    try
    {
      Thread.currentThread().sleep(client.rubbos.getDownRampTime());
    }
    catch (java.lang.InterruptedException ie)
    {
      System.err.println("ClientEmulator has been interrupted.");
    }
    downRampStats.merge(stats);
    endDownRampDate = new GregorianCalendar();

    // Wait for completion
    client.setEndOfSimulation();
    System.out.println("ClientEmulator: Shutting down threads ...<br>");
    for (int i = 0 ; i < client.rubbos.getNbOfClients() ; i++)
    {
      try
      {
        sessions[i].join(2000);
      }
      catch (java.lang.InterruptedException ie)
      {
        System.err.println("ClientEmulator: Thread "+i+" has been interrupted.");
      }
    }
    System.out.println("Done\n");
    endDate = new GregorianCalendar();
    allStats.merge(stats);
    allStats.merge(runSessionStats);
    allStats.merge(upRampStats);
    System.out.println("<p><hr><p>");


    // #############################################
    // ### EXPERIMENT IS OVER, COLLECT THE STATS ###
    // #############################################

    // All clients completed, here is the performance report !
    // but first redirect the output
    try
    {
      PrintStream outputStream;
      if (isMainClient)
        outputStream = new PrintStream(new FileOutputStream(reportDir+"perf.html"));
      else
        outputStream = new PrintStream(new FileOutputStream(args[1]));
      System.setOut(outputStream);
      System.setErr(outputStream);
    }
    catch (Exception e)
    {
      System.out.println("Output redirection failed, displaying results on standard output ("+e.getMessage()+")");
    }

    System.out.println("<center><h2>*** Performance Report ***</h2></center><br>");    
    System.out.println("<A HREF=\"perf.html\">Overall performance report</A><br>");
    System.out.println("<A HREF=\"stat_client0.html\">Main client (localhost) statistics</A><br>");
    for (int i = 0 ; i < client.rubbos.getRemoteClients().size() ; i++)
      System.out.println("<A HREF=\"stat_client"+(i+1)+".html\">client1 ("+client.rubbos.getRemoteClients().get(i)+") statistics</A><br>");

    System.out.println("<p><br>&nbsp&nbsp&nbsp<A HREF=\"perf.html#node\">Node information</A><br>");
    System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#time\">Test timing information</A><br>");
    System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#up_stat\">Up ramp statistics</A><br>");
    System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#run_stat\">Runtime session statistics</A><br>");
    System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#down_stat\">Down ramp statistics</A><br>");
    System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#all_stat\">Overall statistics</A><br>");
    System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#cpu_graph\">CPU usage graphs</A><br>");
    System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#procs_graph\">Processes usage graphs</A><br>");
    System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#mem_graph\">Memory usage graph</A><br>");
    System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#disk_graph\">Disk usage graphs</A><br>");
    System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#net_graph\">Network usage graphs</A><br>");

    if (isMainClient)
    {
      // Get information about each node
      System.out.println("<br><A NAME=\"node\"></A><h3>Node Information</h3><br>");
      try
      {
        File dir = new File(".");
        String nodeInfoProgram = "/bin/echo \"Host  : \"`/bin/hostname` ; " +
            "/bin/echo \"Kernel: \"`/bin/cat /proc/version` ; " +
            "/bin/grep net /proc/pci ; " +
            "/bin/grep processor /proc/cpuinfo ; " +
            "/bin/grep vendor_id /proc/cpuinfo ; " +
            "/bin/grep model /proc/cpuinfo ; " +
            "/bin/grep MHz /proc/cpuinfo ; " +
            "/bin/grep cache /proc/cpuinfo ; " +
            "/bin/grep MemTotal /proc/meminfo ; " +
            "/bin/grep SwapTotal /proc/meminfo ";


        // Web server
        System.out.println("<B>Web server</B><br>");
        String[] cmdWeb = new String[3];
        cmdWeb[0] = client.rubbos.getMonitoringRsh();
        cmdWeb[1] = client.rubbos.getWebServerName();
        cmdWeb[2] = nodeInfoProgram;
        Process p = Runtime.getRuntime().exec(cmdWeb);
        BufferedReader read = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String msg;
        while ((msg = read.readLine()) != null)
          System.out.println(msg+"<br>");
        read.close();
      
        // Database server
        System.out.println("<br><B>Database server</B><br>");
        String[] cmdDB = new String[3];
        cmdDB[0] = client.rubbos.getMonitoringRsh();
        cmdDB[1] = client.rubbos.getDBServerName();
        cmdDB[2] =nodeInfoProgram;
        p = Runtime.getRuntime().exec(cmdDB);
        read = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((msg = read.readLine()) != null)
          System.out.println(msg+"<br>");
        read.close();

        // Client
        System.out.println("<br><B>Local client</B><br>");
        String[] cmdClient = new String[3];
        cmdClient[0] = client.rubbos.getMonitoringRsh();
        cmdClient[1] = "localhost";
        cmdClient[2] = nodeInfoProgram;
        p = Runtime.getRuntime().exec(cmdClient);
        read = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((msg = read.readLine()) != null)
          System.out.println(msg+"<br>");
        read.close();

        // Remote Clients
        for (int i = 0 ; i < client.rubbos.getRemoteClients().size() ; i++)
        {
          System.out.println("<br><B>Remote client "+i+"</B><br>");
          String[] rcmdClient = new String[3];
          rcmdClient[0] = client.rubbos.getMonitoringRsh();
          rcmdClient[1] = (String)client.rubbos.getRemoteClients().get(i);
          rcmdClient[2] = nodeInfoProgram;
          p = Runtime.getRuntime().exec(rcmdClient);
          read = new BufferedReader(new InputStreamReader(p.getInputStream()));
          while ((msg = read.readLine()) != null)
            System.out.println(msg+"<br>");
          read.close();
        }

        PrintStream outputStream = new PrintStream(new FileOutputStream(reportDir+"stat_client0.html"));
        System.setOut(outputStream);
        System.setErr(outputStream);
        System.out.println("<center><h2>*** Performance Report ***</h2></center><br>");    
        System.out.println("<A HREF=\"perf.html\">Overall performance report</A><br>");
        System.out.println("<A HREF=\"stat_client0.html\">Main client (localhost) statistics</A><br>");
        for (int i = 0 ; i < client.rubbos.getRemoteClients().size() ; i++)
          System.out.println("<A HREF=\"stat_client"+(i+1)+".html\">client1 ("+client.rubbos.getRemoteClients().get(i)+") statistics</A><br>");
        System.out.println("<p><br>&nbsp&nbsp&nbsp<A HREF=\"perf.html#node\">Node information</A><br>");
        System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#time\">Test timing information</A><br>");
        System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#up_stat\">Up ramp statistics</A><br>");
        System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#run_stat\">Runtime session statistics</A><br>");
        System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#down_stat\">Down ramp statistics</A><br>");
        System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#all_stat\">Overall statistics</A><br>");
        System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#cpu_graph\">CPU usage graphs</A><br>");
        System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#procs_graph\">Processes usage graphs</A><br>");
        System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#mem_graph\">Memory usage graph</A><br>");
        System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#disk_graph\">Disk usage graphs</A><br>");
        System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#net_graph\">Network usage graphs</A><br>");

      }
      catch (Exception ioe)
      {
        System.out.println("An error occured while getting node information ("+ioe.getMessage()+")");
      }
    }

    // Test timing information
    System.out.println("<br><p><A NAME=\"time\"></A><h3>Test timing information</h3><p>");
    System.out.println("<TABLE BORDER=1>");
    System.out.println("<TR><TD><B>Test start</B><TD>"+TimeManagement.dateToString(startDate));
    System.out.println("<TR><TD><B>Up ramp start</B><TD>"+TimeManagement.dateToString(upRampDate));
    System.out.println("<TR><TD><B>Runtime session start</B><TD>"+TimeManagement.dateToString(runSessionDate));
    System.out.println("<TR><TD><B>Down ramp start</B><TD>"+TimeManagement.dateToString(downRampDate));
    System.out.println("<TR><TD><B>Test end</B><TD>"+TimeManagement.dateToString(endDate));
    System.out.println("<TR><TD><B>Up ramp length</B><TD>"+TimeManagement.diffTime(upRampDate, runSessionDate)+
                       " (requested "+client.rubbos.getUpRampTime()+" ms)");
    System.out.println("<TR><TD><B>Runtime session length</B><TD>"+TimeManagement.diffTime(runSessionDate, downRampDate)+
                       " (requested "+client.rubbos.getSessionTime()+" ms)");
    System.out.println("<TR><TD><B>Down ramp length</B><TD>"+TimeManagement.diffTime(downRampDate, endDownRampDate)+
                       " (requested "+client.rubbos.getDownRampTime()+" ms)");
    System.out.println("<TR><TD><B>Total test length</B><TD>"+TimeManagement.diffTime(startDate, endDate));
    System.out.println("</TABLE><p>");

    // Stats for each ramp
    System.out.println("<br><A NAME=\"up_stat\"></A>");
    upRampStats.display_stats("Up ramp", TimeManagement.diffTimeInMs(upRampDate, runSessionDate), false);
    System.out.println("<br><A NAME=\"run_stat\"></A>");
    runSessionStats.display_stats("Runtime session", TimeManagement.diffTimeInMs(runSessionDate, downRampDate), false);
    System.out.println("<br><A NAME=\"down_stat\"></A>");
    downRampStats.display_stats("Down ramp", TimeManagement.diffTimeInMs(downRampDate, endDownRampDate), false);
    System.out.println("<br><A NAME=\"all_stat\"></A>");
    allStats.display_stats("Overall", TimeManagement.diffTimeInMs(upRampDate, endDownRampDate), false);


    if (isMainClient)
    {
      // Wait for end of all monitors and remote clients
      try
      {
        for (int i = 0 ; i < client.rubbos.getRemoteClients().size() ; i++)
        {
          remoteClientMonitor[i].waitFor();
          remoteClient[i].waitFor();
        }
        webServerMonitor.waitFor();
        dbServerMonitor.waitFor();
      }

      catch (Exception e)
      {
        System.out.println("An error occured while waiting for remote processes termination ("+e.getMessage()+")");
      }
      
      // Generate the graphics 
      try
      {
        String[] cmd = new String[4];
        cmd[0] = "bench/generate_graphs.sh";
        cmd[1] = reportDir;
        cmd[2] = client.rubbos.getGnuPlotTerminal();
        cmd[3] = Integer.toString(client.rubbos.getRemoteClients().size()+1);
        Process graph = Runtime.getRuntime().exec(cmd);
        graph.waitFor();
      }
      catch (Exception e)
      {
        System.out.println("An error occured while generating the graphs ("+e.getMessage()+")");
      }
    }

    System.out.println("<br><A NAME=\"cpu_graph\"></A>");
    System.out.println("<br><h3>CPU Usage graphs</h3><p>");
    System.out.println("<TABLE>");
    System.out.println("<TR><TD><IMG SRC=\"cpu_busy."+client.rubbos.getGnuPlotTerminal()+"\"><TD><IMG SRC=\"client_cpu_busy."+client.rubbos.getGnuPlotTerminal()+"\">");
    System.out.println("<TR><TD><IMG SRC=\"cpu_idle."+client.rubbos.getGnuPlotTerminal()+"\"><TD><IMG SRC=\"client_cpu_idle."+client.rubbos.getGnuPlotTerminal()+"\">");
    System.out.println("<TR><TD><IMG SRC=\"cpu_user_kernel."+client.rubbos.getGnuPlotTerminal()+"\"><TD><IMG SRC=\"client_cpu_user_kernel."+client.rubbos.getGnuPlotTerminal()+"\">");
    System.out.println("</TABLE><p>");

    System.out.println("<br><A NAME=\"procs_graph\"></A>");
    System.out.println("<TABLE>");
    System.out.println("<br><h3>Processes Usage graphs</h3><p>");
    System.out.println("<TR><TD><IMG SRC=\"procs."+client.rubbos.getGnuPlotTerminal()+"\"><TD><IMG SRC=\"client_procs."+client.rubbos.getGnuPlotTerminal()+"\">");
    System.out.println("<TR><TD><IMG SRC=\"ctxtsw."+client.rubbos.getGnuPlotTerminal()+"\"><TD><IMG SRC=\"client_ctxtsw."+client.rubbos.getGnuPlotTerminal()+"\">");
    System.out.println("</TABLE><p>");

    System.out.println("<br><A NAME=\"mem_graph\"></A>");
    System.out.println("<br><h3>Memory Usage graph</h3><p>");
    System.out.println("<TABLE>");
    System.out.println("<TR><TD><IMG SRC=\"mem_usage."+client.rubbos.getGnuPlotTerminal()+"\"><TD><IMG SRC=\"client_mem_usage."+client.rubbos.getGnuPlotTerminal()+"\">");
    System.out.println("<TR><TD><IMG SRC=\"mem_cache."+client.rubbos.getGnuPlotTerminal()+"\"><TD><IMG SRC=\"client_mem_cache."+client.rubbos.getGnuPlotTerminal()+"\">");
    System.out.println("</TABLE><p>");

    System.out.println("<br><A NAME=\"disk_graph\"></A>");
    System.out.println("<br><h3>Disk Usage graphs</h3><p>");
    System.out.println("<TABLE>");
    System.out.println("<TR><TD><IMG SRC=\"disk_rw_req."+client.rubbos.getGnuPlotTerminal()+"\"><TD><IMG SRC=\"client_disk_rw_req."+client.rubbos.getGnuPlotTerminal()+"\">");
    System.out.println("<TR><TD><IMG SRC=\"disk_tps."+client.rubbos.getGnuPlotTerminal()+"\"><TD><IMG SRC=\"client_disk_tps."+client.rubbos.getGnuPlotTerminal()+"\">");
    System.out.println("</TABLE><p>");

    System.out.println("<br><A NAME=\"net_graph\"></A>");
    System.out.println("<br><h3>Network Usage graphs</h3><p>");
    System.out.println("<TABLE>");
    System.out.println("<TR><TD><IMG SRC=\"net_rt_byt."+client.rubbos.getGnuPlotTerminal()+"\"><TD><IMG SRC=\"client_net_rt_byt."+client.rubbos.getGnuPlotTerminal()+"\">");
    System.out.println("<TR><TD><IMG SRC=\"net_rt_pack."+client.rubbos.getGnuPlotTerminal()+"\"><TD><IMG SRC=\"client_net_rt_pack."+client.rubbos.getGnuPlotTerminal()+"\">");
    System.out.println("<TR><TD><IMG SRC=\"socks."+client.rubbos.getGnuPlotTerminal()+"\"><TD><IMG SRC=\"client_socks."+client.rubbos.getGnuPlotTerminal()+"\">");
    System.out.println("</TABLE><p>");


    if (isMainClient)
    {
      // Compute the global stats
      try
      {
        String[] cmd = new String[6];
        cmd[0] = "bench/compute_global_stats.awk";
        cmd[1] = "-v";
        cmd[2] = "path="+reportDir;
        cmd[3] = "-v";
        cmd[4] = "nbscript="+Integer.toString(client.rubbos.getRemoteClients().size()+1);
        cmd[5] = reportDir+"stat_client0.html";
        Process computeStats = Runtime.getRuntime().exec(cmd);
        computeStats.waitFor();
      }
      catch (Exception e)
      {
        System.out.println("An error occured while generating the graphs ("+e.getMessage()+")");
      }
    }

    Runtime.getRuntime().exit(0);
  }

}
