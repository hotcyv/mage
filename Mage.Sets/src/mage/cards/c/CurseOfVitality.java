/*
 *  Copyright 2010 BetaSteward_at_googlemail.com. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY BetaSteward_at_googlemail.com ``AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL BetaSteward_at_googlemail.com OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and should not be interpreted as representing official policies, either expressed
 *  or implied, of BetaSteward_at_googlemail.com.
 */
package mage.cards.c;

import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.AttachEffect;
import mage.abilities.keyword.EnchantAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.GameEvent.EventType;
import mage.game.permanent.Permanent;
import mage.target.TargetPlayer;
import mage.target.targetpointer.FixedTarget;
import java.util.UUID;
import mage.abilities.effects.common.GainLifeEffect;
import mage.abilities.effects.common.GainLifeTargetEffect;
import mage.game.combat.CombatGroup;
import mage.players.Player;

/**
 *
 * @author Saga
 */
public class CurseOfVitality extends CardImpl {

    public CurseOfVitality(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{2}{W}");
        this.subtype.add(SubType.AURA, SubType.CURSE);

        // Enchant player
        TargetPlayer auraTarget = new TargetPlayer();
        this.getSpellAbility().addTarget(auraTarget);
        this.getSpellAbility().addEffect(new AttachEffect(Outcome.Detriment));
        this.addAbility(new EnchantAbility(auraTarget.getTargetName()));

        // Whenever enchanted player is attacked, you gain 2 life. Each opponent attacking that player does the same.
        this.addAbility(new CurseOfVitalityTriggeredAbility());
    }

    public CurseOfVitality(final CurseOfVitality card) {
        super(card);
    }

    @Override
    public CurseOfVitality copy() {
        return new CurseOfVitality(this);
    }
}

class CurseOfVitalityTriggeredAbility extends TriggeredAbilityImpl {

    public CurseOfVitalityTriggeredAbility() {
        super(Zone.BATTLEFIELD, new GainLifeEffect(2), false);
    }

    public CurseOfVitalityTriggeredAbility(final CurseOfVitalityTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == EventType.DECLARED_ATTACKERS;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        Permanent enchantment = game.getPermanentOrLKIBattlefield(getSourceId());
        Player controller = game.getPlayer(getControllerId());
        if (controller != null && enchantment != null
                && enchantment.getAttachedTo() != null
                && game.getCombat().getPlayerDefenders(game).contains(enchantment.getAttachedTo())) {
            for (CombatGroup group : game.getCombat().getBlockingGroups()) {
                if (group.getDefenderId().equals(enchantment.getAttachedTo())) {
                    if (controller.hasOpponent(game.getCombat().getAttackingPlayerId(), game)) {
                        Effect effect = new GainLifeTargetEffect(2);
                        effect.setTargetPointer(new FixedTarget(game.getCombat().getAttackingPlayerId()));
                        this.addEffect(effect);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getRule() {
        return "Whenever enchanted player is attacked, you gain 2 life. Each opponent attacking that player does the same.";
    }

    @Override
    public CurseOfVitalityTriggeredAbility copy() {
        return new CurseOfVitalityTriggeredAbility(this);
    }

}
