package br.com.gamemods.mychunks.data.state;

public interface Modifiable
{
    boolean isModified();
    void setModified(boolean modified);
}
