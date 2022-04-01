package zener.zcomm.gui.zcomm_main;

import zener.zcomm.Main;
import zener.zcomm.chat.ChatHistory;
import zener.zcomm.util.nrCheck;

public class NRConfirm implements Runnable{

    private InputField NR_Field;
    private int comm_nr;

    public NRConfirm(MainGUIDescription mainGUIDescription, int comm_nr, InputField nrfield) {
        this.NR_Field = nrfield;
        this.comm_nr = comm_nr;
    }

    public void run() {
        String text = NR_Field.getText();
        nrCheck nrcheck = new nrCheck(text);

        if (text.trim().equals("G")) {
            NR_Field.setSuggestion(" G");
            NR_Field.setText("");
            return;
        }



        if (!nrcheck.isNr()) {
            NR_Field.setSuggestion("NaN");
            NR_Field.setText("");
            return;
        }

        if (nrcheck.isLengthLong()) {
            NR_Field.setSuggestion(">3");
            NR_Field.setText("");
            return;
        }

        if (nrcheck.isLengthShort()) {
            NR_Field.setSuggestion("<1");
            NR_Field.setText("");
            return;
        }


        if (nrcheck.isNegative()) {
            NR_Field.setSuggestion("-");
            NR_Field.setText("");
            return;
        }

        if (nrcheck.getNr() == Main.GLOBAL_CHANNEL_NR) {
            NR_Field.setSuggestion(" G");
            NR_Field.setText("");
            ChatHistory.getInstance().setLast_channel(comm_nr, Main.GLOBAL_CHANNEL_NR);
            return;
        }

        ChatHistory.getInstance().add_last_channel(comm_nr, nrcheck.getNr());
        ChatHistory.getInstance().setLast_channel(comm_nr, nrcheck.getNr());
        NR_Field.setSuggestion(nrcheck.getNrStr());
        NR_Field.setText("");
    
    }
    
}
