/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
 */
package com.intellij.lang.ant.config.actions;

import com.intellij.lang.ant.AntBundle;
import com.intellij.lang.ant.config.AntConfiguration;
import com.intellij.lang.ant.config.AntConfigurationBase;
import com.intellij.lang.ant.config.AntNoFileException;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AddAntBuildFile extends AnAction {
  public void actionPerformed(AnActionEvent event) {
    final DataContext dataContext = event.getDataContext();
    final Project project = CommonDataKeys.PROJECT.getData(dataContext);
    if (project == null) {
      return;
    }

    final Set<VirtualFile> files = new HashSet<VirtualFile>();
    
    VirtualFile[] contextFiles = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
    if (contextFiles != null) {
      files.addAll(Arrays.asList(contextFiles));
    }
    final VirtualFile singleFile = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);
    if (singleFile != null) {
      files.add(singleFile);
    }

    if (files.isEmpty()) {
      return;
    }
    
    final AntConfiguration antConfiguration = AntConfiguration.getInstance(project);
    int filesAdded = 0;
    final StringBuilder errors = new StringBuilder();

    for (VirtualFile file : files) {
      try {
        antConfiguration.addBuildFile(file);
        filesAdded++;
      }
      catch (AntNoFileException e) {
        String message = e.getMessage();
        if (message == null || message.length() == 0) {
          message = AntBundle.message("cannot.add.build.files.from.excluded.directories.error.message", e.getFile().getPresentableUrl());
        }
        if (errors.length() > 0) {
          errors.append("\n");
        }
        errors.append(message);
      }
    }

    if (errors.length() > 0) {
      Messages.showWarningDialog(project, errors.toString(), AntBundle.message("cannot.add.build.file.dialog.title"));
    }
    if (filesAdded > 0) {
      ToolWindowManager.getInstance(project).getToolWindow(ToolWindowId.ANT_BUILD).activate(null);
    }
  }

  public void update(AnActionEvent e) {
    final DataContext dataContext = e.getDataContext();
    final Presentation presentation = e.getPresentation();
    final Project project = CommonDataKeys.PROJECT.getData(dataContext);
    if (project == null) {
      disable(presentation);
      return;
    }

    final VirtualFile file = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);
    if (file == null) {
      disable(presentation);
      return;
    }

    final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
    if (!(psiFile instanceof XmlFile)) {
      disable(presentation);
      return;
    }

    final XmlFile xmlFile = (XmlFile)psiFile;
    final XmlDocument document = xmlFile.getDocument();
    if (document == null) {
      disable(presentation);
      return;
    }

    final XmlTag rootTag = document.getRootTag();
    if (rootTag == null) {
      disable(presentation);
      return;
    }

    if (!"project".equals(rootTag.getName())) {
      disable(presentation);
      return;
    }

    if (AntConfigurationBase.getInstance(project).getAntBuildFile(psiFile) != null) {
      disable(presentation);
      return;
    }

    enable(presentation);
  }

  private static void enable(Presentation presentation) {
    presentation.setEnabled(true);
    presentation.setVisible(true);
  }

  private static void disable(Presentation presentation) {
    presentation.setEnabled(false);
    presentation.setVisible(false);
  }
}

