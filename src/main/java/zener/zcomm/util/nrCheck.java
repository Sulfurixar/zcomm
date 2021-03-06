package zener.zcomm.util;

import org.apache.commons.lang3.math.NumberUtils;

import net.minecraft.server.MinecraftServer;
import zener.zcomm.Main;
import zener.zcomm.components.ComponentHandler;

public class nrCheck {
    
    private String nr;
    private Integer _nr;
    private boolean length_long;
    private boolean length_short;

    public nrCheck(String nr) {
        this.nr = nr;
        if (nr == null) {
            return;
        }

        if (NumberUtils.isParsable(nr)) {
            this._nr = Integer.parseInt(nr);
        }

        this.length_long = nr.length() > 3;

        this.length_short = nr.length() < 1;

    }

    public nrCheck(int nr) {

        this.nr = String.format("%03d", nr);
        this._nr = nr;
        this.length_long = this.nr.length() > 3;
        this.length_short = this.nr.length() < 1;
    }



    public boolean isNr() {
        return (NumberUtils.isParsable(nr));
    }

    public boolean isLengthLong() {
        return this.length_long;
    }

    public boolean isLengthShort() {
        return this.length_short;
    }

    public boolean isNegative() {
        return this._nr < 0;
    }

    public boolean nrTaken(MinecraftServer server) {
        boolean checknr = ComponentHandler.isNrFree(server, _nr);
        return (!checknr || _nr == Main.GLOBAL_CHANNEL_NR);
    }

    public int getNr() {
        if (this._nr == null && isValid()) {
            return Main.GLOBAL_CHANNEL_NR;
        }
        return this._nr;
    }

    public String getNrStr() {
        return this.nr;
    }

    public boolean isValid() {
        return (isNr() && !isLengthLong() && !isLengthShort() && !isNegative()) || nr.equals("G");
    }
}
