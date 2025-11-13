package io.mtr.rage.adapter;

import java.util.List;

/**
 * @author MTR
 */

public interface RageAdapter {

    /**
     * Determines the title of the sidebar
     *
     * @return the title
     */
    String getTitle();

    /**
     * Determines the lines of the sidebar
     *
     * @return the lines
     */
    List<String> getLines();
}
