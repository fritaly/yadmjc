/**
 * Provides the game's foundation classes.
 *
 * <h1>Directions</h1>
 *
 * <table cellspacing="1" cellpadding="5" border="1">
 * <tr>
 * <td width="33%" align="center">&nbsp;</td>
 * <td width="33%" align="center">North</td>
 * <td width="33%" align="center">&nbsp;</td>
 * </tr>
 * <tr>
 * <td width="33%" align="center">West</td>
 * <td width="33%" align="center">&nbsp;</td>
 * <td width="33%" align="center">East</td>
 * </tr>
 * <tr>
 * <td width="33%" align="center">&nbsp;</td>
 * <td width="33%" align="center">South</td>
 * <td width="33%" align="center">&nbsp;</td>
 * </tr>
 * </table>
 * <h1>Locations</h1>
 * <table cellspacing="1" cellpadding="5" border="1">
 * <tr>
 * <td width="50%" align="center">Front<br>Left</td>
 * <td width="50%" align="center">Front<br>Right</td>
 * </tr>
 * <tr>
 * <td width="50%" align="center">Rear<br>Left</td>
 * <td width="50%" align="center">Rear<br>Right</td>
 * </tr>
 * </table>
 *
 * <h1>Sectors</h1>
 * <table cellspacing="1" cellpadding="5" border="1">
 * <tr>
 * <td width="50%" align="center">North<br>West</td>
 * <td width="50%" align="center">North<br>East</td>
 * </tr>
 * <tr>
 * <td width="50%" align="center">South<br>West</td>
 * <td width="50%" align="center">South<br>East</td>
 * </tr>
 * </table>
 *
 * Direction + Location = Sector<br>
 * <br>
 * Example: NORTH + FRONT_LEFT = NORTH_WEST.
 */
package fr.ritaly.dungeonmaster;
