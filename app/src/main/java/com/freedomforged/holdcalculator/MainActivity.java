package com.freedomforged.holdcalculator;

import android.app.Activity;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Range;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /* Get the hold entry */
    public void getEntry(View view) {
        EditText hdg_to_fix_view = findViewById(R.id.hdg_to_fix_input);
        EditText hold_hdg_view = findViewById(R.id.hold_hdg_input);
        TextView entry_type_vew = findViewById(R.id.entry_type_output);
        TextView teardrop_heading_view = findViewById(R.id.teardrop_heading_output);
        CheckBox is_left_turn_view = findViewById(R.id.is_left_turn);

        Integer hdg_to_fix = Integer.parseInt(hdg_to_fix_view.getText().toString());
        Integer hold_hdg = Integer.parseInt(hold_hdg_view.getText().toString());
        Boolean left_turn = is_left_turn_view.isChecked();

        String entry = calcEntry(hdg_to_fix, hold_hdg, left_turn);
        String teardrop_heading;
        if (entry.equals("Teardrop")) {
            teardrop_heading = teardropHeading(hold_hdg, left_turn).toString();
        } else {
            teardrop_heading = "N/A";
        }

        entry_type_vew.setText(entry);
        teardrop_heading_view.setText(teardrop_heading);
    }

    /* Actually do the calculations and return the entry */
    private String calcEntry(Integer hdg_to_fix, Integer hold_hdg, Boolean left_turn) {
        if (hold_hdg == 0) {
            hold_hdg = 360; // Let's just fix this right out of the gate
        }
        Integer cfactor = northCorrectionFactor(hold_hdg); // Bring things to 360 based if not already
        Integer htf_recip = recipHeading(hdg_to_fix) + cfactor; // Reciprocal of the heading to fix. Where are we coming FROM. Correction built in.
        if (htf_recip < 1) {
            htf_recip += 360;
        }
        Integer angle_difference = angleDifference(htf_recip, left_turn);

        String entry;
        /* Define Ranges */
        Range parallel_range = Range.create(71, 180);
        Range teardrop_range = Range.create(-179,-110);
        /* End Range Defs */

        if (parallel_range.contains(angle_difference)) {
            entry = "Parallel";
        } else if (teardrop_range.contains(angle_difference)) {
            entry = "Teardrop";
        } else {
            entry = "Direct";
        }
        return entry;
    }

    /* Get the reciprocal heading - mostly for finding where coming from */
    private Integer recipHeading (Integer heading) {
        Integer recip;
        if(heading <= 180) {
            recip = heading + 180;
        } else {
            recip = heading - 180;
        }

        if (recip == 0) {
            recip = 360; // Normalize to 360
        }
        return recip;
    }

    /* Get the angle difference between the corrected recip angle and 360 - Negate for left turns*/
    private Integer angleDifference (Integer htf_recip, Boolean left_turn) {
        Integer angle_diff;
        Integer raw_diff = 360 - htf_recip;

        if (raw_diff <= 180) { // "Left side"
            angle_diff = raw_diff;
        } else { // "Right side"
            angle_diff = raw_diff - 360;
        }

        if (angle_diff == 360) {
            angle_diff = 0;
        }

        if (left_turn) {
            angle_diff *= -1;  // Swap things for left turns. Then only requires one set of ranges
        }

        if (angle_diff == -180) {
            angle_diff = 180;
        }
        return angle_diff;
    }

    /* Correction to bring Hold Heading to 360 */
    private Integer northCorrectionFactor (Integer hold_hdg) {
        Integer diff = 360 - hold_hdg; // This way the diff will always be positive (or 0)
        Integer cfactor = 0;

        if (diff == 360 || diff == 0) {
            cfactor = 0;
        } else if (diff <= 180) { // On the "left" side
            cfactor = diff;
        } else { // On the "right" side
            cfactor = diff - 360;
        }

        return cfactor;
    }

    /* Get the teardrop heading */
    private Integer teardropHeading (Integer hold_hdg, Boolean left_turn) {
        Integer teardrop_heading;
        if (left_turn) {
            teardrop_heading = hold_hdg + 30;
            teardrop_heading = teardrop_heading > 360 ? teardrop_heading - 360 : teardrop_heading;
        } else {
            teardrop_heading = hold_hdg - 30;
            teardrop_heading = teardrop_heading <= 0 ? 360 - (teardrop_heading * -1) : teardrop_heading;
        }

        return teardrop_heading;
    }
}
