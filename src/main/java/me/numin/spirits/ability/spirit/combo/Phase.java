package me.numin.spirits.ability.spirit.combo;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ParticleEffect;
import java.util.ArrayList;
import me.numin.spirits.Spirits;
import me.numin.spirits.ability.api.SpiritAbility;
import me.numin.spirits.utilities.Methods;
import me.numin.spirits.utilities.Methods.SpiritType;
import me.numin.spirits.utilities.Removal;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Phase extends SpiritAbility implements AddonAbility, ComboAbility {

  //TODO: Implement config for new variables. Variables are already made, just need the config paths.
  //TODO: Feature where flying through entities while phased could give the target and/or spirit effects (shivers, etc)
  //TODO: Update sounds.

  private GameMode originGM;
  private Location origin;
  private Removal removal;

  private boolean applyLevitationCD, applyVanishCD, isPhased, playEffects;
  private double multiplier, levitationMultiplier, vanishMultiplier;
  private int minHealth, range;
  private long cooldown, duration, time;

  public Phase(Player player) {
    super(player);

    if (!bPlayer.canBendIgnoreBinds(this)) {
      return;
    }

    setFields();
    if (player.getHealth() <= minHealth) {
      return;
    }

    this.time = System.currentTimeMillis();
    start();
  }

  private void setFields() {
    this.multiplier = Spirits.plugin.getConfig().getLong("Abilities.Spirits.Neutral.Combo.Phase.CooldownMultiplier");
    this.duration = Spirits.plugin.getConfig().getLong("Abilities.Spirits.Neutral.Combo.Phase.Duration");
    this.range = Spirits.plugin.getConfig().getInt("Abilities.Spirits.Neutral.Combo.Phase.Range");
    this.minHealth = Spirits.plugin.getConfig().getInt("Abilities.Spirits.Neutral.Combo.Phase.MinHealth");
    this.applyVanishCD = Spirits.plugin.getConfig().getBoolean("Abilities.Spirits.Neutral.Combo.Phase.Vanish.ApplyCooldown");
    this.vanishMultiplier = Spirits.plugin.getConfig().getLong("Abilities.Spirits.Neutral.Combo.Phase.Vanish.CooldownMultiplier");

    applyLevitationCD = true;
    levitationMultiplier = 4;

    this.origin = player.getLocation();
    this.originGM = player.getGameMode();
    this.isPhased = false;
    this.playEffects = true;
    this.removal = new Removal(player);
  }

  @Override
  public void progress() {
    this.cooldown = (long) ((System.currentTimeMillis() - time) * multiplier);

    if (removal.stop()) {
      remove();
      return;
    }
    setGameMode();
    if (playEffects) {
      playEffects = false;
      playEffects();
    }
    if (System.currentTimeMillis() > time + duration || origin.distanceSquared(player.getLocation()) > range * range) {
      playEffects();
      remove();
      return;
    }
    if (player.isSneaking() && isPhased) {
      playEffects();
      remove();
    }
  }

  @Override
  public void remove() {
    resetGameMode();

    // Cooldown calculations
    long duration = System.currentTimeMillis() - time;
    if (applyVanishCD) {
      long vanishCooldown = (long) (duration * vanishMultiplier);
      bPlayer.addCooldown("Vanish", vanishCooldown);
    }
    if (applyLevitationCD) {
      long levitationCooldown = (long) (duration * levitationMultiplier);
      bPlayer.addCooldown("Levitation", levitationCooldown);
    }
    bPlayer.addCooldown(this);
    super.remove();
  }

  private void setGameMode() {
    player.setGameMode(GameMode.SPECTATOR);
    isPhased = true;
  }

  private void resetGameMode() {
    player.setGameMode(originGM);
    isPhased = false;
  }

  private void playEffects() {
    ParticleEffect.PORTAL.display(player.getLocation().add(0, 1, 0), 0, 0, 0, (int) 1.5F, 100);
    Methods.playSpiritParticles(player, player.getLocation().add(0, 1, 0), 1, 1, 1, 0, 20);
    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5F, -1);
  }

  @Override
  public Object createNewComboInstance(Player player) {
    return new Phase(player);
  }

  @Override
  public ArrayList<AbilityInformation> getCombination() {
    ArrayList<AbilityInformation> combo = new ArrayList<AbilityInformation>();
    combo.add(new AbilityInformation("Vanish", ClickType.LEFT_CLICK));
    combo.add(new AbilityInformation("Vanish", ClickType.LEFT_CLICK));
    combo.add(new AbilityInformation("Possess", ClickType.SHIFT_DOWN));
    combo.add(new AbilityInformation("Possess", ClickType.SHIFT_UP));
    combo.add(new AbilityInformation("Vanish", ClickType.LEFT_CLICK));
    return combo;
  }

  @Override
  public long getCooldown() {
    return cooldown;
  }

  @Override
  public Location getLocation() {
    return player.getLocation();
  }

  @Override
  public String getName() {
    return "Phase";
  }

  @Override
  public String getDescription() {
    return Methods.setSpiritDescription(SpiritType.NEUTRAL, "Combo") +
        Spirits.plugin.getConfig().getString("Language.Abilities.Spirit.Phase.Description");
  }

  @Override
  public String getInstructions() {
    return Methods.getSpiritColor(SpiritType.NEUTRAL) +
        Spirits.plugin.getConfig().getString("Language.Abilities.Spirit.Phase.Instructions");
  }

  @Override
  public String getAuthor() {
    return Methods.getSpiritColor(SpiritType.NEUTRAL) + "" + Methods.getAuthor();
  }

  @Override
  public String getVersion() {
    return Methods.getSpiritColor(SpiritType.NEUTRAL) + Methods.getVersion();
  }

  @Override
  public boolean isEnabled() {
    return Spirits.plugin.getConfig().getBoolean("Abilities.Spirits.Neutral.Combo.Phase.Enabled");
  }

  @Override
  public boolean isExplosiveAbility() {
    return false;
  }

  @Override
  public boolean isHarmlessAbility() {
    return false;
  }

  @Override
  public boolean isIgniteAbility() {
    return false;
  }

  @Override
  public boolean isSneakAbility() {
    return false;
  }

  @Override
  public void load() {
  }

  @Override
  public void stop() {
  }
}