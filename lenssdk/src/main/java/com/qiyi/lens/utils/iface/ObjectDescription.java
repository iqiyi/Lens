/*
 *
 * Copyright (C) 2020 iQIYI (www.iqiyi.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.qiyi.lens.utils.iface;

public class ObjectDescription implements IObjectDescriptor {
    public Object value;
    public String objectDescription;

    public ObjectDescription(Object var) {
        this.value = var;

    }

    public ObjectDescription(Object var, String des) {
        this.value = var;
        this.objectDescription = des;

    }

    private String parseDescription(String var){
        if(var == null) return "";
        return var;
    }


    @Override//what we need to display
    public String toString(Object object) {
        if(value instanceof String) {
            return value +" "+ parseDescription(objectDescription);
        }
        return value.getClass().getSimpleName() + " " + parseDescription(objectDescription);
    }

    @Override
    public String getTag(Object object) {
        return null;
    }

    @Override
    public String toString() {
        return toString(this);
    }

    public Object getValue() {
        return value;
    }
}
