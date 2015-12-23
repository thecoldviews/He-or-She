addpath(genpath('C:\Users\Sarthak\Dropbox\Matlab Workspace\PR Project'));

folder_path = 'C:\Users\New User\Dropbox\Matlab Workspace\PR Project\';

femaleDirName = 'audio_data\female\';
femaleDir = dir(femaleDirName);     femaleDir = femaleDir(3:end);

TrainingDataMfcc=[];
TrainingDataSpec=[];
TrainingDataBoth=[];
TrainingDataPitch=[];
TrainingLabels=[];
TestingDataMfcc=[];
TestingDataSpec=[];
TestingDataBoth=[];
TestingDataPitch=[];
TestingLabels=[];



for i = 1:floor(size(femaleDir, 1)/2)
    femaleDir(floor(i)).name
    audioSubfolder = dir([femaleDirName, femaleDir(i).name]);
    audioSubfolder = audioSubfolder(3:end);
    for j = 1:size(audioSubfolder, 1),
        audioSubfolder(j)
        [signal,fs] = wavread([femaleDirName, femaleDir(i).name,'\', audioSubfolder(j).name]);
        featureVector=extract_features(signal,fs,'');
        size(featureVector)
        TrainingDataBoth=[TrainingDataBoth;featureVector];
        TrainingDataSpec=[TrainingDataSpec;featureVector(:,118:123)];
        TrainingDataMfcc=[TrainingDataMfcc;featureVector(:,1:117)];
        TrainingDataPitch=[TrainingDataPitch;featureVector(:,123)];
        TrainingLabels=[TrainingLabels, ones(1,size(featureVector,1))];
    end
end

for i = floor(size(femaleDir, 1)/2)+1:floor(size(femaleDir, 1))
    femaleDir(floor(i)).name
    audioSubfolder = dir([femaleDirName, femaleDir(i).name]);
    audioSubfolder = audioSubfolder(3:end);
    for j = 1:size(audioSubfolder, 1),
        audioSubfolder(j)
        [signal,fs] = wavread([femaleDirName, femaleDir(i).name,'\', audioSubfolder(j).name]);
        featureVector=extract_features(signal,fs,'');
        size(featureVector)
        TestingDataBoth=[TestingDataBoth;featureVector];
        TestingDataSpec=[TestingDataSpec;featureVector(:,118:123)];
        TestingDataMfcc=[TestingDataMfcc;featureVector(:,1:117)];
        TestingDataPitch=[TestingDataPitch;featureVector(:,123)];
        TestingLabels=[TestingLabels, ones(1,size(featureVector,1))];
    end
end

maleDirName = 'audio_data\male\';
maleDir = dir(maleDirName);     maleDir = maleDir(3:end);

for i = 1:floor(size(maleDir, 1)/2),
    maleDir(i).name
    audioSubfolder = dir([maleDirName, maleDir(i).name]);
    audioSubfolder = audioSubfolder(3:end);
    for j = 1:size(audioSubfolder, 1),
        audioSubfolder(j)
        [signal,fs] = wavread([maleDirName, maleDir(i).name,'\', audioSubfolder(j).name]);
        featureVector=extract_features(signal,fs,'');
        TrainingDataBoth=[TrainingDataBoth;featureVector];
        TrainingDataSpec=[TrainingDataSpec;featureVector(:,118:123)];
        TrainingDataMfcc=[TrainingDataMfcc;featureVector(:,1:117)];
        TrainingDataPitch=[TrainingDataPitch;featureVector(:,123)];
        TrainingLabels=[TrainingLabels, zeros(1,size(featureVector,1))];
    end
end


for i = floor(size(maleDir, 1)/2)+1:floor(size(maleDir, 1))
    maleDir(floor(i)).name
    audioSubfolder = dir([maleDirName, maleDir(i).name]);
    audioSubfolder = audioSubfolder(3:end);
    for j = 1:size(audioSubfolder, 1),
        audioSubfolder(j)
        [signal,fs] = wavread([maleDirName, maleDir(i).name,'\', audioSubfolder(j).name]);
        featureVector=extract_features(signal,fs,'');
        TestingDataBoth=[TestingDataBoth;featureVector];
        TestingDataSpec=[TestingDataSpec;featureVector(:,118:123)];
        TestingDataMfcc=[TestingDataMfcc;featureVector(:,1:117)];
        TestingDataPitch=[TestingDataPitch;featureVector(:,123)];
        TestingLabels=[TestingLabels, zeros(1,size(featureVector,1))];
    end
end

svmdegreeboth=svmtrain(TrainingLabels',TrainingDataBoth,'-q -t 1')
svmdegreemfcc=svmtrain(TrainingLabels',TrainingDataMfcc,'-q -t 1')
svmdegreespec=svmtrain(TrainingLabels',TrainingDataSpec,'-q -t 1')
svmdegreepitch=svmtrain(TrainingLabels',TrainingDataPitch,'-q -t 1')
svmlinearboth=svmtrain(TrainingLabels',TrainingDataBoth,'-q -t 0')
svmlinearmfcc=svmtrain(TrainingLabels',TrainingDataMfcc,'-q -t 0')
svmlinearspec=svmtrain(TrainingLabels',TrainingDataSpec,'-q -t 0')
svmlinearpitch=svmtrain(TrainingLabels',TrainingDataPitch,'-q -t 0')
svmrbfboth=svmtrain(TrainingLabels',TrainingDataBoth,'-q -t 2')
svmrbfmfcc=svmtrain(TrainingLabels',TrainingDataMfcc,'-q -t 2')
svmrbfspec=svmtrain(TrainingLabels',TrainingDataSpec,'-q -t 2')
svmrbfpitch=svmtrain(TrainingLabels',TrainingDataPitch,'-q -t 2')