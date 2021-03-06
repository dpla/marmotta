/*
 * MediaEntryModule.java
 *
 * Created on April 19, 2006, 1:15 AM
 *
 * This code is currently released under the Mozilla Public License.
 * http://www.mozilla.org/MPL/
 *
 * Alternately you may apply the terms of the Apache Software License
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rometools.feed.module.mediarss;

import org.rometools.feed.module.mediarss.types.MediaContent;
import org.rometools.feed.module.mediarss.types.MediaGroup;


/**
 * Represents entry/item level information.
 * @author cooper
 */
public interface MediaEntryModule extends MediaModule {
    /**
     * Returns the MediaContent items for the entry.
     * @return Returns the MediaContent items for the entry.
     */
    public MediaContent[] getMediaContents();

    /**
     * Returns the media groups for the entry.
     * @return Returns the media groups for the entry.
     */
    public MediaGroup[] getMediaGroups();
}
