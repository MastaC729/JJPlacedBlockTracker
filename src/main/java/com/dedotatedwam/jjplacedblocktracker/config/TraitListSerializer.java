package com.dedotatedwam.jjplacedblocktracker.config;


import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.Map;

public class TraitListSerializer implements TypeSerializer<TraitList> {

	@Override
	public TraitList deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
		TraitList traitList = new TraitList();
		for (Map.Entry<Object, ? extends ConfigurationNode> entry : value.getChildrenMap().entrySet()) {
			traitList.put((String) entry.getKey(), entry.getValue().getValue());
		}
		return traitList;
	}

	@Override
	public void serialize(TypeToken<?> type, TraitList obj, ConfigurationNode value) throws ObjectMappingException {
		for (Map.Entry<String, Object> entry : obj.getTraits().entrySet()) {
			value.getNode(entry.getKey()).setValue(entry.getValue());
		}
	}

}
