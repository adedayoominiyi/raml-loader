/*
 * Copyright © 2015 Stefan Niederhauser (nidin@gmx.ch)
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
package guru.nidi.loader.apidesigner;


import guru.nidi.loader.repository.RepositoryEntry;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class ApiDesignerFile implements RepositoryEntry {
    private String name;
    private String path;
    private String contents;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String getContent() {
        return getContents();
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        try {
            this.contents = URLDecoder.decode(contents, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public String toString() {
        return "ApiDesignerFile{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
