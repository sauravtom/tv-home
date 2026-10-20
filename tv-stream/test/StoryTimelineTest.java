import in.xagi.tvstream.StoryTimeline;
public class StoryTimelineTest {
    static void eq(int actual,int expected){if(actual!=expected)throw new AssertionError(actual+" != "+expected);}
    public static void main(String[] args){
        double[] starts={0,6.5,13.4};
        eq(StoryTimeline.beatAt(starts,0),0);
        eq(StoryTimeline.beatAt(starts,6.49),0);
        eq(StoryTimeline.beatAt(starts,6.5),1);
        eq(StoryTimeline.beatAt(starts,42),2);
        eq(StoryTimeline.beatAt(starts,3),0); // backward seek
        eq(StoryTimeline.beatAt(new double[0],3),-1);
        eq(StoryTimeline.imageAt(new int[]{4,5},10,8,10),4);
        eq(StoryTimeline.imageAt(new int[]{4,5},10,8,14),5);
        eq(StoryTimeline.imageAt(new int[]{4,5},10,8,99),5);
        eq(StoryTimeline.imageAt(new int[0],0,1,0),-1);
        System.out.println("Timeline boundary and seek tests passed");
    }
}
