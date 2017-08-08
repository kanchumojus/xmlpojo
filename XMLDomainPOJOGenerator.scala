package com

import scala.collection.mutable._
import scala.xml._



class XMLDomainPOJOGenerator {
  var inputPathForLoadingXML = "/usr/local/tools/ea/RPOSXMLMapper/data/pretty/";
  var outputPathForWritingJavaClasses = "/usr/local/tools/ea/RPOSXMLMapper/data/pretty/";
}

object XMLDomainPOJOGenerator {

  /**
   * This object holds the reference for all the utility methods defined in XMLPojoUtility class
   */
  var utility = new XMLPojoUtility;

  def main(arg: Array[String]) {
    var xmlDomain = new XMLDomainPOJOGenerator;

    var files = utility.getListOfFilesFromDirectory(xmlDomain.inputPathForLoadingXML, "xml_pretty");

    var allTagsMap = new HashMap[String, TagInfo];
    for (f <- files) {
      var xml = XML.loadFile(f);
      allTagsMap=buildTagInfo(allTagsMap, xml);
    } //end of looping through each file


    utility.constructPOJOClassGeneration(allTagsMap, xmlDomain.outputPathForWritingJavaClasses);

  } //end of main

  def buildTagInfo(allTagsMap: HashMap[String, TagInfo], node: Node): HashMap[String, TagInfo] = {
    var localCount = new HashMap[String, Int]();
    var nodeName = node.label;
    var tagInfo= allTagsMap.getOrElse(nodeName, new TagInfo(nodeName));

    node.attributes.asAttrMap.foreach(f => {
      tagInfo.attributes += f._1
    });

    allTagsMap.put(node.label, tagInfo);
    for (child <- node.child.filter(!_.isAtom)) {
      if(utility.isSimpleTag(child)){
        tagInfo.simpleTag += child.label;
      } else {
        tagInfo.complexTag+=child.label;
      }
      var currentNodeCount=localCount.getOrElse(child.label, 0)+1;
      localCount.put(child.label, currentNodeCount);
      buildTagInfo(allTagsMap, child);
    }
    for(nodeName<-localCount.keySet){
      var tagInfo=allTagsMap.get(nodeName).get
      tagInfo.maxCounts=(tagInfo.maxCounts max localCount.get(nodeName).get)
    }
    allTagsMap;
  }

}