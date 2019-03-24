package org.jenkinsci.plugins.ewm.casc;

import hudson.ExtensionList;
import jenkins.model.Jenkins;
import jenkins.model.GlobalNodePropertiesConfiguration;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.jenkinsci.plugins.ewm.definitions.DiskPool;
import org.jenkinsci.plugins.ewm.steps.ExwsAllocateStep;
import org.jenkinsci.plugins.ewm.steps.ExwsStep;
import org.jenkinsci.plugins.ewm.definitions.Template;
import org.jenkinsci.plugins.ewm.nodes.ExternalWorkspaceProperty;
import org.jenkinsci.plugins.ewm.nodes.NodeDisk;
import org.jenkinsci.plugins.ewm.nodes.NodeDiskPool;
import org.junit.ClassRule;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.jvnet.hudson.test.JenkinsRule;
import io.jenkins.plugins.casc.ConfigurationAsCode;
import io.jenkins.plugins.casc.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Test for Configuration As Code Compatibility.
 *
 * @author Martin d'Anjou
 */
public class ConfigAsCodeTest {

    @ClassRule public static JenkinsRule r = new JenkinsRule();

    @Test
    public void shouldSupportConfigurationAsCode() throws Exception {
        URL resource = ConfigAsCodeTest.class.getResource("configuration-as-code.yaml");
        String config = resource.toString();
        ConfigurationAsCode.get().configure(config);

        //GlobalNodePropertiesConfiguration globalNodeProperties = ExtensionList.lookupSingleton(GlobalNodePropertiesConfiguration.class);
        //ExternalWorkspaceProperty.DescriptorImpl exwsNodeProperty = ExtensionList.lookupSingleton(GlobalNodePropertiesConfiguration.Descriptor.class);
        //System.out.println(globalNodeProperties);
        //System.out.println(globalNodeProperties.getDescriptor());
        /*
        List<NodeDiskPool> nodeDiskPools = exwsNodeProperty.getNodeDiskPools();
        assertThat(nodeDiskPools.size(), is(1));
        assertThat(nodeDiskPools.get(0).getDiskPoolRefId(), is("master-node-id"));
        NodeDisk nodeDisk = nodeDiskPools.get(0).getNodeDisks().get(0);
        assertThat(nodeDisk.getDiskRefId(), is("master-node-disk"));
        assertThat(nodeDisk.getNodeMountPoint(), is("/tmp/master-node"));
*/

        Template template;
        NodeDiskPool nodeDiskPool;
        List<DiskPool> diskPools;
        DiskPool diskPool;
        NodeDisk nodeDisk;

        ExwsAllocateStep.DescriptorImpl descriptor =  ExtensionList.lookupSingleton(ExwsAllocateStep.DescriptorImpl.class);
        diskPools = descriptor.getDiskPools();
        diskPool = diskPools.get(0);
        assertThat(diskPool.getDiskPoolId(), is("diskpool1"));
        assertThat(diskPool.getDisplayName(), is("diskpool1 display name"));
        assertThat(diskPool.getDisks().get(0).getDiskId(), is("disk1"));
        assertThat(diskPool.getDisks().get(0).getDisplayName(), is("disk one display name"));
        assertThat(diskPool.getDisks().get(0).getMasterMountPoint(), is("/tmp"));

        ExwsStep.DescriptorImpl globalTemplateDescriptor = ExtensionList.lookupSingleton(ExwsStep.DescriptorImpl.class);
        List<Template> templates = globalTemplateDescriptor.getTemplates();

        template = findTemplateByLabel(templates, "all");
        assertNotNull(template);
        assertThat(template.getNodeDiskPools().size(), is(2));
        nodeDiskPool = template.getNodeDiskPools().get(0);
        System.out.println(nodeDiskPool);
        System.out.println(nodeDiskPool.getDiskPoolRefId());
        nodeDiskPool = template.getNodeDiskPools().get(1);
        System.out.println(nodeDiskPool);
        System.out.println(nodeDiskPool.getDiskPoolRefId());

        nodeDiskPool = findNodeDiskPoolByRefId(template.getNodeDiskPools(), "dp1");
        assertNotNull(nodeDiskPool);

        nodeDisk = findNodeDiskByRefId(nodeDiskPool.getNodeDisks(), "dp1refid1");
        assertNotNull(nodeDisk);
        assertThat(nodeDisk.getNodeMountPoint(), is("/tmp/template11"));
        nodeDisk = findNodeDiskByRefId(nodeDiskPool.getNodeDisks(), "dp1refid2");
        assertNotNull(nodeDisk);
        assertThat(nodeDisk.getNodeMountPoint(), is("/tmp/template12"));

        nodeDiskPool = findNodeDiskPoolByRefId(template.getNodeDiskPools(), "dp2");
        assertNotNull(nodeDiskPool);

        nodeDisk = findNodeDiskByRefId(nodeDiskPool.getNodeDisks(), "dp2refid1");
        assertNotNull(nodeDisk);
        assertThat(nodeDisk.getNodeMountPoint(), is("/tmp/template21"));
    }

    /**
     * Find a node disk by its ref id.
     * @param nodeDisks the list of node disks
     * @param refId the refId of the disk to find in the list of node disks
     * @return the nodeDisk instance when found, or null when not found
     */
    private NodeDisk findNodeDiskByRefId(List<NodeDisk> nodeDisks, String refId) {
        for(NodeDisk nodeDisk : nodeDisks) {
            if (nodeDisk.getDiskRefId().equals(refId)) {
                return nodeDisk;
            }
        }
        return null;
    }

    /**
     * Find a node disk pool by its ref id.
     * @param nodeDiskPools the list of node disk pools
     * @param refId the refId of the node disk pool to find in the list of node disk pools
     * @return the nodeDiskPool instance when found, or null when not found
     */
    private NodeDiskPool findNodeDiskPoolByRefId(List<NodeDiskPool> nodeDiskPools, String refId) {
        System.out.println("Looking for: "+refId);
        for (NodeDiskPool nodeDiskPool : nodeDiskPools) {
            System.out.println("Node disk poll ref id: "+nodeDiskPool);
            System.out.println("Node disk poll ref id: "+nodeDiskPool.getDiskPoolRefId());
            if (nodeDiskPool.getDiskPoolRefId().equals(refId)) {
                return nodeDiskPool;
            }
        }
        return null;
    }

    /**
     * Find a template by its label.
     * @param templates the list of templates
     * @param label the label of the template to be found
     * @return the template instance that matches the label, or null when not found
     */
    private Template findTemplateByLabel(List<Template> templates, String label) {
        for (Template template : templates) {
            if (template.getLabel().equals(label)) {
                return template;
            }
        }
        return null;
    }
/*
    @Test
    public void exportConfiguration() throws Exception {
        Yaml yaml = new Yaml();

        // get the CasC configure
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        ConfigurationAsCode.get().export(outstream);
        ByteArrayInputStream instream = new ByteArrayInputStream(outstream.toByteArray());
        Map<String, Object> exportMap = (Map<String, Object>) yaml.load(instream);

        // get the yaml configure
        File file  = new File(ConfigAsCodeTest.class.getResource("configuration-as-code.yaml").getFile());
        FileInputStream fileInputStream = new FileInputStream(file);
        Map<String, Object> yamlMap = (Map<String, Object>) yaml.load(fileInputStream);

        // assert
        System.out.println("yaml unclassified: "+yamlMap.get("unclassified"));
        System.out.println("exported unclassified: "+exportMap.get("unclassified"));
        System.out.println("yaml jenkins: "+yamlMap.get("jenkins"));
        System.out.println("exported unclassified: "+exportMap.get("jenkins"));
        assertEquals(yamlMap.get("unclassified"), exportMap.get("unclassified"));
    }
    */
}
