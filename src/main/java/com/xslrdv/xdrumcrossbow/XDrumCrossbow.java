/*
 * X Drum Crossbow - Vanilla-friendly and balanced automatic Drum Crossbow with an arrow magazine, and instant firing with no draw delay.
 * Copyright (C) 2026 xslrdv
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.xslrdv.xdrumcrossbow;

import com.xslrdv.xdrumcrossbow.network.ReloadPayload;
import com.xslrdv.xdrumcrossbow.network.ShootPayload;
import com.xslrdv.xdrumcrossbow.registry.ModComponents;
import com.xslrdv.xdrumcrossbow.registry.ModCreativeTabs;
import com.xslrdv.xdrumcrossbow.registry.ModEntities;
import com.xslrdv.xdrumcrossbow.registry.ModItems;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(XDrumCrossbow.MODID)
public class XDrumCrossbow {
    public static final String MODID = "xdrumcrossbow";

    public XDrumCrossbow(IEventBus modEventBus) {
        ModComponents.register(modEventBus);
        ModItems.register(modEventBus);
        ModEntities.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        modEventBus.addListener(XDrumCrossbow::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToServer(ReloadPayload.TYPE, ReloadPayload.CODEC, ReloadPayload::handle);
        registrar.playToServer(ShootPayload.TYPE, ShootPayload.CODEC, ShootPayload::handle);
    }
}
