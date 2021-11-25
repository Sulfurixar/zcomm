package zener.zcomm.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import lombok.Getter;
import net.minecraft.entity.player.PlayerEntity;
import zener.zcomm.Main;

public class tData {

    public final UUID UUID;
    @Getter private byte[] HASH;
    public final boolean isTechnician;
    public final boolean isHeadTechnician;
    private final String p = Main.ZCOMM_COMMUNICATION_IDENTIFIER.toString();

    public tData(PlayerEntity player) {
        this.UUID = player.getUuid();
        this.isTechnician = false;
        this.isHeadTechnician = false;

        try {
            this.HASH = this.makeHash();
        } catch (NoSuchAlgorithmException e) {}
    }

    public tData(PlayerEntity player, byte t) {
        this.UUID = player.getUuid();
        if (t == 0){
            this.isHeadTechnician = false;
            this.isTechnician = false;
        } else if (t == 1) {
            this.isHeadTechnician = false;
            this.isTechnician = true;
        } else {
            this.isHeadTechnician = true;
            this.isTechnician = false;
        }

        try {
            this.HASH = this.makeHash();
        } catch (NoSuchAlgorithmException e) {}
    }

    public tData(byte[] a, byte[] b) {
        String s = new String(a, StandardCharsets.UTF_8);
        this.UUID = java.util.UUID.fromString(s);
        this.HASH = new byte[b.length-1];
        System.arraycopy(b, 0, this.HASH, 0, b.length-1);
        if (b[b.length-1] == 0){
            this.isHeadTechnician = false;
            this.isTechnician = false;
        } else if (b[b.length-1] == 1) {
            this.isHeadTechnician = false;
            this.isTechnician = true;
        } else {
            this.isHeadTechnician = true;
            this.isTechnician = false;
        }
    }

    public List<Byte[]> getBytes() {
        Byte[] a = ArrayUtils.toObject(this.UUID.toString().getBytes());
        Byte[] b = new Byte[this.HASH.length + 1];
        System.arraycopy(ArrayUtils.toObject(this.HASH), 0, b, 0, this.HASH.length);
        b[b.length-1] = 0;
        if (this.isHeadTechnician) {
            b[b.length-1] = 2;
        } else if (this.isTechnician) {
            b[b.length-1] = 1;
        }
        List<Byte[]> c = new ArrayList<>();
        c.add(a);
        c.add(b);
        return c;
    }

    private byte[] makeHash() throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(this.p.getBytes());
        byte[] digest = md.digest();
        byte[] uuid = this.UUID.toString().getBytes();
        byte[] combined = new byte[digest.length + uuid.length];

        System.arraycopy(uuid,0,combined,0,uuid.length);
        System.arraycopy(digest,0,combined,uuid.length, digest.length);
        md.update(combined);
        return md.digest();

    }
    
}
