package com

import scala.xml._
import scala.collection._
import java.io.{ File, PrintWriter }
import scala.collection.mutable.HashMap;

class XMLPojoUtility {

  def constructPOJOClassGeneration(allTagsMap: HashMap[String, TagInfo], outputPath: String) {

    for (tagInfoMap <- allTagsMap) {

      var className = tagInfoMap._1;
      var tagInfo = tagInfoMap._2;
      var simpleTags = tagInfo.simpleTag;

      var singleRepeatedTag = Set[String]();
      var multiRepeatedTag = Set[String]();

      tagInfo.complexTag.foreach(tag => {
        if (simpleTags.contains(tag)) {
          simpleTags.remove(tag);
        }
        if (allTagsMap.contains(tag) && allTagsMap(tag).maxCounts > 1) {
          multiRepeatedTag += tag
        } else {
          singleRepeatedTag += tag;
        }
      })

      if (singleRepeatedTag.size > 0 || multiRepeatedTag.size > 0 || simpleTags.size > 0) {

        //This string holds the entire class or template
        var skeletonForClass = new StringBuilder;
        skeletonForClass ++= s"""
      |public class ${className} extends RPOSXMLModel  {
      |public ${className} (Node ${className}Node){
      |super(${className}Node);
      |}
      """.stripMargin

        //This string holds the setSimpleTagMethod functionality  
        var setSimpleTagMethod = new StringBuilder;
        setSimpleTagMethod ++= s"""
      |@Override
      |void setSimpleTags() {
      |
      """.stripMargin

        //This string holds the setNodeChildren functionality
        var setNodeChildren = new StringBuilder;
        setNodeChildren ++= s"""
       |@Override
       |void setNodeChildren() {
       """.stripMargin

        //This string holds the serializeMethod functionality
        var serializeMethod = new StringBuilder;
        serializeMethod ++= s"""
        |@Override
        |public String serialize(){
        |StringBuilder sb=new StringBuilder();
        |sb.append("${className}\\n");
        |
        """.stripMargin

        //Start of simpleTags loop
        for (name <- simpleTags) {
          if (name.toUpperCase() contains "DATE") {
            skeletonForClass ++= s"""|public Date ${name};""".stripMargin;

            setSimpleTagMethod ++= s"""${name}=getTagValueAsDate("${name}","yyyy-MM-dd'T'HH:mm:ss.SSS");""";
          } else {
            skeletonForClass ++= s"""public String ${name};""";

            setSimpleTagMethod ++= s"""${name}=getTagValueAsString("${name}");""";
          }
          serializeMethod ++= s"""serializeAttribute(sb, "${name}",${name});""";
        } //end of simpleTags for-each 

        setSimpleTagMethod ++= "}"

        //This string holds the setAttributeMethod functionality
        var setAttributeMethod = new StringBuilder;
        setAttributeMethod ++= s"""
            |@Override
            |void setAttributes() {
            """.stripMargin
        //start of attributes loop
        for (attr <- tagInfo.attributes) {
          setAttributeMethod ++= s"""${attr} =getAttributeValue("${attr}");"""

          serializeMethod ++= s"""|serializeAttribute(sb, "${attr}", ${attr});
           """.stripMargin;

          skeletonForClass ++= s"""|public String ${attr};""".stripMargin;
        }
        setAttributeMethod ++= "}";

        //Start of Complextags loop
        for (node <- multiRepeatedTag) {

          skeletonForClass ++= s"private java.util.List<${node}> ${node}s;"

          setNodeChildren ++= s"""
        |${node}s = new ArrayList<>();
        |List<Node> ${node}Nodes = getChildNodes("${node}");
        |if(${node}Nodes != null)
        |{
        |for(Node ${node}Node : ${node}Nodes) {
        |${node}s.add(new ${node}(${node}Node));
        |}
        |}
        |
        """.stripMargin;

          serializeMethod ++= s"""
           |for(${node} ${node}: ${node}s ) {
           |serializeNode(sb, ${node});
           |}
           |
           """.stripMargin;
        } //end of complexTags for-each

        //Start of singleRepeatedTag loop
        for (node <- singleRepeatedTag) {

          skeletonForClass ++= s"""|private ${node} ${node};""".stripMargin;

          setNodeChildren ++= s"""
        |Node ${node}Node = getChildNode("${node}");
        |this.${node} = (${node}Node !=null) ? new ${node}(${node}Node) :null;
        |
        """.stripMargin;

          serializeMethod ++= s"""|serializeNode(sb, ${node});""".stripMargin;
        } //end of singleRepeatedTag foreach 

        setNodeChildren ++= "}";
        serializeMethod ++= s"""|return sb.toString();}""".stripMargin;

        skeletonForClass ++= s"""
          |$setSimpleTagMethod
          |
          |$setNodeChildren
          |
          |$setAttributeMethod
          |
          |$serializeMethod
          |
          |}
          """.stripMargin

        val data = Array(skeletonForClass.toString());
        printToFile(new File(outputPath + className + ".java")) {
          p => data.foreach(p.println);
        };

        //TODO: Remove this, if you are running on production
        println(skeletonForClass.toString());
        println("----------------");

      }
    }

  }

  //To check if the current node is simple tag, i.e., if it has only text as its child
  //eg:<DomainName>gpshospitality.com</DomainName>
  def isSimpleTag(node: Node) =
    (node.child.size == 1 && node.child(0).isInstanceOf[Text]) || node.text.toString().isEmpty();

  //To print the contents to file
  def printToFile(f: File)(op: PrintWriter => Unit) {
    val p = new PrintWriter(f);
    try {
      op(p);
    } finally {
      p.close();
    }
  } //end of printToFile

  /**
   * to load multiple files from a directory,
   * this loads only files with extension *.xml
   */
  def getListOfFilesFromDirectory(dir: String, extensions: String): List[File] = {
    val directory = new File(dir);
    directory.listFiles.filter(_.isFile()).toList.filter(file => file.getName.endsWith(extensions));
  } //end of getListOfFiles

}