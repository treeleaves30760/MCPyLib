package com.mcpylib.plugin;

public class CommandResult {

    private final boolean success;
    private final Object data;
    private final String error;

    private CommandResult(boolean success, Object data, String error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static CommandResult success(Object data) {
        return new CommandResult(true, data, null);
    }

    public static CommandResult error(String error) {
        return new CommandResult(false, null, error);
    }

    public boolean isSuccess() {
        return success;
    }

    public Object getData() {
        return data;
    }

    public String getError() {
        return error;
    }
}
