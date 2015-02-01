package uk.co.thefishlive;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.lesscss.LessCompiler;
import org.lesscss.LessException;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "less-compile", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class LessCompileMojo extends AbstractMojo {

    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "${project.build.directory}/classes", property = "outputDir", required = true)
    private File outputDirectory;

    @Parameter(property = "extensions", required = true)
    private List<String> extensions;

    @Component
    private MavenProject project;

    private LessCompiler compiler;
    private int count = 0;

    public void execute() throws MojoExecutionException {
        List resources = project.getResources();
        compiler = new LessCompiler();

        for (Object object : resources) {
            Resource resource = (Resource) object;

            getLog().info(resource.getDirectory());

            File dir = new File(resource.getDirectory());
            processDirectory(dir, "");
        }

        getLog().info("Compiled " + count + " Less files to Css");
    }

    public void processDirectory(File dir, String path) throws MojoExecutionException {
        for (File current : dir.listFiles()) {
            if (current.isDirectory()) {
                processDirectory(current, path + File.separator + current.getName());
                continue;
            }

            if (!extensions.contains(current.getName().substring(current.getName().lastIndexOf('.') + 1))) {
                continue;
            }

            File output = new File(outputDirectory, path + File.separator + current.getName().substring(0, current.getName().lastIndexOf('.')) + ".css");

            try {
                getLog().debug("Compiled " + current.toString() + " to " + output.toString());
                compiler.compile(current, output);
                count++;
            } catch (IOException | LessException e) {
                throw new MojoExecutionException("Error compiling " + current.toString(), e);
            }
        }
    }
}
