package br.com.gamemods.mychunks.data.api;

public class DataStorageException extends Exception
{
    public DataStorageException(String message)
    {
        super(message);
    }

    public DataStorageException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DataStorageException(Throwable cause)
    {
        super(cause);
    }
}
