/*
 * Copyright (c) 2012-2014 Chenren Xu, Sugang Li
 * Acknowledgments: Yanyong Zhang, Yih-Farn (Robin) Chen, Emiliano Miluzzo, Jun Li
 * Contact: lendlice@winlab.rutgers.edu
 *
 * This file is part of the Crowdpp.
 *
 * Crowdpp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Crowdpp is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Crowdpp. If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.example.dd;

import edu.rutgers.winlab.crowdpp.util.Maths;
import edu.rutgers.winlab.crowdpp.util.Constants;
import edu.rutgers.winlab.crowdpp.util.Distances;
import edu.rutgers.winlab.crowdpp.util.FileProcess;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.Math;

import org.ejml.simple.SimpleMatrix;

import android.util.Log;

/**
 * The SpeakerCount class 
 * @author Chenren Xu, Sugang Li
 */
public class SpeakerCount {
	
	/** the chosen distance function */
	public static double getDistance(SimpleMatrix a, SimpleMatrix b) {
		return Distances.Cosine(a, b);
	}

	/** gender estimation algorithm */
	public static int getGender(double pitch) {
		// uncertain
		int gender = 0;	
		// male		
		if (pitch < Constants.pitch_male_upper) {
			gender = 1;	
		}
		// female
		else if (pitch > Constants.pitch_female_lower) {
			gender = 2;	
		}
		return gender;
	}
	
	/** identify if the speakers are the same one or not based on gender information  
	 * 	@return 1: same gender 
	 * 					0: different gender
	 * 				 -1: uncertain
	 */
	public static int genderDecision(double pitch_a, double pitch_b) {
		int gender_a = getGender(pitch_a);
		int gender_b = getGender(pitch_b);
		// clear gender identification through voice
		if (gender_a > 0 && gender_b > 0) {
			if (gender_a == gender_b)
				return 1;
			else 
				return 0;
		}
		// leave this job to MFCC
		return -1;
	}
		
	@SuppressWarnings("rawtypes")
	/** segment the conversation testing data */	
	public static List[] segmentation(String[] args) throws java.io.IOException {

		double[][] mfcc 	= FileProcess.readFile(args[0]);
		double[][] pitch 	= FileProcess.readFile(args[1]);			
		
		// compute the number of segments
		int sample_num 		= pitch.length;
		double[] time			= new double[sample_num]; 
		time[0] = 0.032;
		for(int i = 1; i < time.length; i++) {
			time[i] = time[i-1] + 0.016;
		}
		
		int end_id = sample_num - 1;
		int seg_num = (int) Math.floor(time[end_id] / Constants.seg_duration_sec);

		if (seg_num == 0)
			return null;
		
		// find the begin and end indices of each segment
		int[] lower_id = new int [seg_num];
		int[] upper_id = new int [seg_num];

		lower_id[0] = 0;
		int seg_id = 1;
			
		for (int i = 0; i < sample_num; i++) {
			if (time[i] <= (double)(seg_id * Constants.seg_duration_sec) && time[i+1] > (double)(seg_id * Constants.seg_duration_sec)) {
				upper_id[seg_id-1] = i;
				seg_id++;
				if (seg_id == seg_num + 1) 
					break;
				lower_id[seg_id-1] = i + 1;
			}
		}
			
		// filter out the non-voiced segments		
		SimpleMatrix mfcc_mat = new SimpleMatrix(mfcc);
		List<SimpleMatrix> mfcc_list = new ArrayList<SimpleMatrix>();
		List<Double> pitch_list = new ArrayList<Double>();
		List<Double> pitch_rate = new ArrayList<Double>();		
		List<Double> pitch_mu = new ArrayList<Double>();
		List<Double> pitch_sigma = new ArrayList<Double>();
			
		for (int i = 0; i < seg_num; i++) {
			int c = 0;
			List<Double> temp_pitch = new ArrayList<Double>();
			for (int j = lower_id[i]; j < upper_id[i]; j++) {
				if (pitch[j][0] < Constants.pitch_mu_lower || pitch[j][0] > Constants.pitch_mu_upper) 
					pitch[j][0] = -1;				
				if (pitch[j][0] != -1) {
					c++;
					temp_pitch.add(pitch[j][0]);
				}
			}
			pitch_rate.add((double)c / (upper_id[i] - lower_id[i] + 1));
			pitch_mu.add(Maths.getMean(temp_pitch));
			pitch_sigma.add(Math.sqrt(Maths.getVariance(temp_pitch)));
			if (pitch_rate.get(i) >= Constants.pitch_rate_lower 
					&& pitch_mu.get(i) >= Constants.pitch_mu_lower 
					&& pitch_mu.get(i) <= Constants.pitch_mu_upper 
					&& pitch_sigma.get(i) <= Constants.pitch_sigma_upper) {
				mfcc_list.add(mfcc_mat.extractMatrix(lower_id[i], upper_id[i], 0, 19)); 
				pitch_list.add(pitch_mu.get(i));
			}
		}
		
		// no voiced data
		if (pitch_list.size() == 0) {
			return null;
		}
		
		// iteratively pre-cluster the neighbor segments until no merging happens 
		int last_size, p, q;
		while (true) {
			last_size = mfcc_list.size();
			p = 0; q = 1;
			while (q < mfcc_list.size()) {
		  	if (getDistance(mfcc_list.get(p), mfcc_list.get(q)) <= Constants.mfcc_dist_same_un && genderDecision(pitch_list.get(p), pitch_list.get(q)) == 1) {	
		  		mfcc_list.set(p, mfcc_list.get(p).combine(mfcc_list.get(p).numRows(), 0, mfcc_list.get(q)));
		      pitch_list.set(p, (pitch_list.get(p) + pitch_list.get(q)) / 2);
		      mfcc_list.remove(q);
		      pitch_list.remove(q);
		    }
		    else {
		    	p = q; q++;
		    }
		  }
		  if (last_size == mfcc_list.size()) {
		  	break;
		  }
		}	
			
		return new List[]{mfcc_list, pitch_list};
	}
	
	/** unsupervised speaker counting algorithm without owner's calibration data */	
	public static int unsupervisedAlgorithm(List<SimpleMatrix> mfcc, List<Double> pitch) {

	  List<SimpleMatrix> new_mfcc = new ArrayList<SimpleMatrix>();
	  // admit the first segment as speaker 1
	  new_mfcc.add(mfcc.get(0));
	  List<Double> new_pitch = new ArrayList<Double>();
	  new_pitch.add(pitch.get(0));
		int speaker_count = 1;

	  for (int i = 1; i < mfcc.size(); i++) {
	  	int diff_count = 0;
	    for (int j = 0; j < speaker_count; j++) {
	    	// for each audio segment i, compare it with the each admitted audio segment j
	    	double mfcc_dist = getDistance(mfcc.get(i), new_mfcc.get(j));
	    	// different gender
	      if (genderDecision(pitch.get(i), new_pitch.get(j)) == 0) { 
	      	diff_count = diff_count + 1;
	      } 
	      // mfcc distance is larger than a threshold
	      else if (mfcc_dist >= Constants.mfcc_dist_diff_un) {
	      	diff_count = diff_count + 1;            	
	      }
	      // same speaker
	      else {
	      	if (mfcc_dist <= Constants.mfcc_dist_same_un && genderDecision(pitch.get(i), new_pitch.get(j)) == 1) {
		        new_mfcc.set(j, new_mfcc.get(j).combine(new_mfcc.get(j).numRows(), 0, mfcc.get(i))); // merge
						break;
		      }
	      }
	    }
	    // admit as a new speaker if different from all the admitted speakers.
	    if (diff_count == speaker_count) {
	    	speaker_count = speaker_count + 1;
	      new_mfcc.add(mfcc.get(i));
	      new_pitch.add(pitch.get(i));
	    }
	  }
	  return speaker_count;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	/** unsupervised speaker counting algorithm wrapper function */	
	public static int unsupervised(String[] test_files) throws java.io.IOException {

		List[] features = segmentation(test_files);
		
		if (features == null) {
			Log.i("SpeakerCount", "No enough audio data");
			return 0;
		}
		else {
			return unsupervisedAlgorithm(features[0], features[1]);
		}
	}
	
}