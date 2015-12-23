function SFM = spectral_entropy(windowFFT)
% 
% function En = feature_spectral_entropy(windowFFT, numOfShortBlocks)
% 
% This function computes the spectral entropy of the given audio frame
%
% ARGUMENTS:
% - windowFFT:       the abs(FFT) of an audio frame
%                    (computed by getDFT() function)
% - numOfShortBins   the number of bins in which the spectrum
%                    is divided
%
% RETURNS:
% - En:              the value of the spectral entropy
%


geoMean = exp(mean(log(windowFFT),1)); % Mean along each column
arithMean = mean(windowFFT,1); % Mean along each column

SFM = geoMean ./ arithMean; % Spectral flatness measure for each window in x