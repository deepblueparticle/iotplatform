/*******************************************************************************
 * Copyright 2017 osswangxining@163.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
/**
 * Copyright © 2016-2017 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.iotp.infomgt.dao.model.sql;

import static org.iotp.infomgt.dao.model.ModelConstants.ATTRIBUTE_KEY_COLUMN;
import static org.iotp.infomgt.dao.model.ModelConstants.ATTRIBUTE_TYPE_COLUMN;
import static org.iotp.infomgt.dao.model.ModelConstants.BOOLEAN_VALUE_COLUMN;
import static org.iotp.infomgt.dao.model.ModelConstants.DOUBLE_VALUE_COLUMN;
import static org.iotp.infomgt.dao.model.ModelConstants.ENTITY_ID_COLUMN;
import static org.iotp.infomgt.dao.model.ModelConstants.ENTITY_TYPE_COLUMN;
import static org.iotp.infomgt.dao.model.ModelConstants.LAST_UPDATE_TS_COLUMN;
import static org.iotp.infomgt.dao.model.ModelConstants.LONG_VALUE_COLUMN;
import static org.iotp.infomgt.dao.model.ModelConstants.STRING_VALUE_COLUMN;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.iotp.infomgt.dao.model.ToData;
import org.iotp.infomgt.data.common.ThingType;
import org.iotp.infomgt.data.kv.AttributeKvEntry;
import org.iotp.infomgt.data.kv.BaseAttributeKvEntry;
import org.iotp.infomgt.data.kv.BooleanDataEntry;
import org.iotp.infomgt.data.kv.DoubleDataEntry;
import org.iotp.infomgt.data.kv.KvEntry;
import org.iotp.infomgt.data.kv.LongDataEntry;
import org.iotp.infomgt.data.kv.StringDataEntry;

import lombok.Data;

@Data
@Entity
@Table(name = "attribute_kv")
@IdClass(AttributeKvCompositeKey.class)
public class AttributeKvEntity implements ToData<AttributeKvEntry>, Serializable {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = ENTITY_TYPE_COLUMN)
    private ThingType entityType;

    @Id
    @Column(name = ENTITY_ID_COLUMN)
    private String entityId;

    @Id
    @Column(name = ATTRIBUTE_TYPE_COLUMN)
    private String attributeType;

    @Id
    @Column(name = ATTRIBUTE_KEY_COLUMN)
    private String attributeKey;

    @Column(name = BOOLEAN_VALUE_COLUMN)
    private Boolean booleanValue;

    @Column(name = STRING_VALUE_COLUMN)
    private String strValue;

    @Column(name = LONG_VALUE_COLUMN)
    private Long longValue;

    @Column(name = DOUBLE_VALUE_COLUMN)
    private Double doubleValue;

    @Column(name = LAST_UPDATE_TS_COLUMN)
    private Long lastUpdateTs;

    @Override
    public AttributeKvEntry toData() {
        KvEntry kvEntry = null;
        if (strValue != null) {
            kvEntry = new StringDataEntry(attributeKey, strValue);
        } else if (booleanValue != null) {
            kvEntry = new BooleanDataEntry(attributeKey, booleanValue);
        } else if (doubleValue != null) {
            kvEntry = new DoubleDataEntry(attributeKey, doubleValue);
        } else if (longValue != null) {
            kvEntry = new LongDataEntry(attributeKey, longValue);
        }
        return new BaseAttributeKvEntry(kvEntry, lastUpdateTs);
    }
}
