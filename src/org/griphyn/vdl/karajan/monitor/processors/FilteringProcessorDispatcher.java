/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 7, 2012
 */
package org.griphyn.vdl.karajan.monitor.processors;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.griphyn.vdl.karajan.monitor.SystemState;

public class FilteringProcessorDispatcher implements LogMessageProcessor {
    private Level level;
    private Map<String, AbstractFilteringProcessor> processors;
    private SystemState state;
    
    public FilteringProcessorDispatcher() {
        processors = new HashMap<String, AbstractFilteringProcessor>();
    }
    
    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public void add(AbstractFilteringProcessor p) {
        processors.put(p.getMessageHeader(), p);
        p.initialize(state);
    }

    @Override
    public void processMessage(SystemState state, Object message, Object details) {
        SimpleParser p = new SimpleParser(String.valueOf(message));
        
        p.skipWhitespace();
        String header = p.word();
        AbstractFilteringProcessor proc = processors.get(header);
        if (proc != null) {
            p.skipWhitespace();
            proc.processMessage(state, p, details);
        }
    }

    @Override
    public String getSupportedSourceName() {
        return "swift";
    }

    @Override
    public Level getSupportedLevel() {
        return level;
    }

    @Override
    public void initialize(SystemState state) {
        this.state = state;
    }
}
