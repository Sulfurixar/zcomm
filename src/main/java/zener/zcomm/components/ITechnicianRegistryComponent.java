package zener.zcomm.components;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import lombok.Data;
import net.minecraft.world.World;
import zener.zcomm.Main;

public interface ITechnicianRegistryComponent extends ComponentV3 {

    Map<UUID, Technician> getTechnicians();
    World getWorld();

    @Data
    public class Technician {
        boolean technician;
        boolean headTechnician;
        private static final String p = Main.ZCOMM_COMMUNICATION_IDENTIFIER.toString();
        private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

        public Technician() {
            technician = true;
            headTechnician = false;
        }

        public Technician setHeadTechnician() {
            headTechnician = true;
            return this;
        }

        public Technician setHeadTechnician(boolean value) {
            headTechnician = value;
            return this;
        }

        public Technician(boolean headTechnician) {
            this.headTechnician = headTechnician;
        }

        public Technician(boolean technician, boolean headTechnician) {
            this.technician = technician;
            this.headTechnician = headTechnician;
        }

        private static byte[] makeHash(UUID UUID) throws NoSuchAlgorithmException {

            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(p.getBytes());
            byte[] digest = md.digest();
            byte[] uuid = UUID.toString().getBytes();
            byte[] combined = new byte[digest.length + uuid.length];
    
            System.arraycopy(uuid,0,combined,0,uuid.length);
            System.arraycopy(digest,0,combined,uuid.length, digest.length);
            md.update(combined);
            return md.digest();
        }

        private static String bytesToHex(byte[] bytes) {
            byte[] hexChars = new byte[bytes.length * 2];
            for (int j = 0; j < bytes.length; j++) {
                int v = bytes[j] & 0xFF;
                hexChars[j * 2] = HEX_ARRAY[v >>> 4];
                hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
            }
            return new String(hexChars, StandardCharsets.UTF_8);
        }
    
        public static String get_hash(UUID UUID) {
    
            try {
                return bytesToHex(makeHash(UUID));
            } catch (NoSuchAlgorithmException e) {
                return "ERROR";
            }
    
        }
    }

}
