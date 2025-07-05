package ru.worm.discord.chill.youtube;

import ru.worm.discord.chill.util.TextUtil;

public class LoadResult {
    private final String errorMessage;

    public static LoadResult err(String err) {
        return new LoadResult(err);
    }

    public static LoadResult success() {
        return new LoadResult();
    }

    public LoadResult(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LoadResult() {
        this.errorMessage = null;
    }

    public boolean isSuccess() {
        return TextUtil.isEmpty(errorMessage);
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
