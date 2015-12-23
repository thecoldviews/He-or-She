function mfcc_vector = feature_extr_mfcc(sample,fs)
mfc = melcepst(sample, fs, '0dD');
mfc(any(mfc == 0,2),:) = [];   
wentr = [];
for i = 1: size(mfc,2)
   wentr = [wentr, wentropy(mfc(:,i),'shannon')];
end
mfcc_vector = [mean(mfc,1), std(mfc,0,1), wentr];