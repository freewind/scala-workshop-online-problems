package controllers;

import play.mvc.Controller;
import play.Play;

public class Diagnostic extends Controller {

    public static void version()  {
      String version = Play.configuration.get("app.version").toString();
      renderText(version);
    }

    public static void appName()  {
      String appName = Play.configuration.get("application.name").toString();
      renderText(appName);
    }
}
