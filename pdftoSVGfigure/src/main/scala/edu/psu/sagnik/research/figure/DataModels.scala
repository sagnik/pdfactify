package edu.psu.sagnik.research.figure

/**
  * Created by sagnik on 10/1/16.
  */

import edu.psu.sagnik.research.allenaiconversion.AllenAIWord
import edu.psu.sagnik.research.pdsimplify.model.Rectangle
import edu.psu.sagnik.research.pdsimplify.path.model.PDSegment
import org.allenai.pdffigures2.{Box, WordwithBB}


case class CiteSeerXFigure(
                   bb: Rectangle,
                   textWords: Seq[AllenAIWord],
                   caption: Option[String],
                   mention: Option[String],
                   pageNo: Int,
                   pdLines: Seq[PDSegment],
                   pageHeight: Float,
                   pageWidth: Float,
                   dpi: Int,
                   id: String
                 )