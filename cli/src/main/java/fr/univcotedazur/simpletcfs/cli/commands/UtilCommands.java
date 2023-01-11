package fr.univcotedazur.simpletcfs.cli.commands;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class UtilCommands {

    @ShellMethod("Write text on console (echo \"TEXT TO PRINT\")")
    public String echo(String text) {
        return text;
    }

}
